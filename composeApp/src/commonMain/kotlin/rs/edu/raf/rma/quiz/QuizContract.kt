package rs.edu.raf.rma.quiz

data class QuizState(
    val isGenerating: Boolean = true,
    val notEnoughMovies: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val answerRevealed: Boolean = false,
    val correctCount: Int = 0,
    val timeRemainingSeconds: Int = 60,
    val showAbandonDialog: Boolean = false,
) {
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
    val totalQuestions: Int get() = questions.size
    val timeUsedSeconds: Int get() = 60 - timeRemainingSeconds
    val score: Float get() {
        if (totalQuestions == 0) return 0f
        val raw = correctCount * (9f + timeRemainingSeconds / 60f)
        return raw.coerceAtMost(100f)
    }
}

sealed class QuizEvent {
    data class SelectAnswer(val answer: String) : QuizEvent()
    data object ShowAbandonDialog : QuizEvent()
    data object DismissAbandonDialog : QuizEvent()
    data object ConfirmAbandon : QuizEvent()
}

sealed class QuizSideEffect {
    data class NavigateToResult(
        val score: Float,
        val correctCount: Int,
        val totalQuestions: Int,
        val timeUsedSeconds: Int,
    ) : QuizSideEffect()
    data object NavigateBack : QuizSideEffect()
}
