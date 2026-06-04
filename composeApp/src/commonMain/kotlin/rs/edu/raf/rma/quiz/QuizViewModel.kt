package rs.edu.raf.rma.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.movies.data.MovieRepository

private const val REVEAL_DELAY_MS = 1000L
private const val TIMER_SECONDS = 60

class QuizViewModel(
    private val appDatabase: AppDatabase,
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state = _state.asStateFlow()

    private fun setState(reducer: QuizState.() -> QuizState) = _state.getAndUpdate(reducer)

    private val _effects = MutableSharedFlow<QuizSideEffect>()
    val effects = _effects.asSharedFlow()

    private var timerJob: Job? = null

    fun setEvent(event: QuizEvent) {
        viewModelScope.launch {
            when (event) {
                QuizEvent.StartQuiz -> generateQuiz()
                is QuizEvent.SelectAnswer -> onAnswerSelected(event.answer)
                QuizEvent.ShowAbandonDialog -> setState { copy(showAbandonDialog = true) }
                QuizEvent.DismissAbandonDialog -> setState { copy(showAbandonDialog = false) }
                QuizEvent.ConfirmAbandon -> {
                    timerJob?.cancel()
                    setState { copy(showAbandonDialog = false) }
                    _effects.emit(QuizSideEffect.NavigateBack)
                }
            }
        }
    }

    private fun generateQuiz() {
        viewModelScope.launch {
            setState { copy(isNotStarted = false, isGenerating = true) }
            val generator = QuizGenerator(appDatabase.movieDao())
            if (!generator.canStart()) {
                setState { copy(isGenerating = false, notEnoughMovies = true) }
                return@launch
            }
            val questions = generator.generate()
            if (questions.isEmpty()) {
                setState { copy(isGenerating = false, notEnoughMovies = true) }
                return@launch
            }
            setState { copy(isGenerating = false, questions = questions) }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0) {
                delay(1000)
                setState { copy(timeRemainingSeconds = timeRemainingSeconds - 1) }
            }
            finishSession()
        }
    }

    private suspend fun onAnswerSelected(answer: String) {
        val current = _state.value
        if (current.answerRevealed || current.currentQuestion == null) return

        val correct = current.currentQuestion!!.correctAnswerString()
        val isCorrect = answer == correct
        setState {
            copy(
                selectedAnswer = answer,
                answerRevealed = true,
                correctCount = if (isCorrect) correctCount + 1 else correctCount,
            )
        }

        delay(REVEAL_DELAY_MS)

        val nextIndex = current.currentIndex + 1
        if (nextIndex >= current.totalQuestions) {
            finishSession()
        } else {
            setState { copy(currentIndex = nextIndex, selectedAnswer = null, answerRevealed = false) }
        }
    }

    private suspend fun finishSession() {
        timerJob?.cancel()
        val s = _state.value
        val score = s.score

        repository.insertQuizSession(
            score = score,
            correctAnswers = s.correctCount,
            totalQuestions = s.totalQuestions,
            timeUsedSeconds = s.timeUsedSeconds,
            playedAt = System.currentTimeMillis(),
        )
        repository.submitQuizResult(score)

        _effects.emit(
            QuizSideEffect.NavigateToResult(
                score = score,
                correctCount = s.correctCount,
                totalQuestions = s.totalQuestions,
                timeUsedSeconds = s.timeUsedSeconds,
            )
        )
    }
}
