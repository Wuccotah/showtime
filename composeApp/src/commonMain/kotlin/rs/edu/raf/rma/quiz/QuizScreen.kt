package rs.edu.raf.rma.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

private val BgDark = Color(0xFF14181C)
private val BgCard = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)
private val CorrectGreen = Color(0xFF2ECC71)
private val WrongRed = Color(0xFFE74C3C)
private val NeutralBtn = Color(0xFF2C3440)

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = koinViewModel(),
    onNavigateToResult: (score: Float, correct: Int, total: Int, timeUsed: Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is QuizSideEffect.NavigateToResult ->
                    onNavigateToResult(effect.score, effect.correctCount, effect.totalQuestions, effect.timeUsedSeconds)
                QuizSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    BackHandler(enabled = !state.isNotStarted) {
        viewModel.setEvent(QuizEvent.ShowAbandonDialog)
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setEvent(QuizEvent.DismissAbandonDialog) },
            title = { Text("Abandon quiz?", color = TextPrimary) },
            text = { Text("Your progress will be lost.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { viewModel.setEvent(QuizEvent.ConfirmAbandon) }) {
                    Text("Abandon", color = WrongRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setEvent(QuizEvent.DismissAbandonDialog) }) {
                    Text("Keep playing", color = AccentGreen)
                }
            },
            containerColor = BgCard,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
    ) {
        when {
            state.isNotStarted -> {
                QuizStartScreen(onStart = { viewModel.setEvent(QuizEvent.StartQuiz) })
            }

            state.isGenerating -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentGreen,
                )
            }

            state.notEnoughMovies -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Not enough movies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Browse the catalog first to populate your quiz pool.",
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            state.currentQuestion != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // timer + progress header
                    QuizHeader(
                        timeRemaining = state.timeRemainingSeconds,
                        currentIndex = state.currentIndex,
                        total = state.totalQuestions,
                        onClose = { viewModel.setEvent(QuizEvent.ShowAbandonDialog) },
                    )

                    AnimatedContent(
                        targetState = state.currentIndex,
                        transitionSpec = {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        },
                        label = "question",
                    ) { index ->
                        val question = state.questions.getOrNull(index) ?: return@AnimatedContent
                        QuestionCard(
                            question = question,
                            selectedAnswer = state.selectedAnswer,
                            answerRevealed = state.answerRevealed,
                            onAnswer = { viewModel.setEvent(QuizEvent.SelectAnswer(it)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizHeader(timeRemaining: Int, currentIndex: Int, total: Int, onClose: () -> Unit) {
    val timerFraction = timeRemaining / 60f
    val timerColor = when {
        timerFraction > 0.5f -> AccentGreen
        timerFraction > 0.25f -> Color(0xFFF39C12)
        else -> WrongRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${currentIndex + 1} / $total",
                color = TextMuted,
                fontSize = 13.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$timeRemaining s",
                    color = timerColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Abandon quiz",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { timerFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = timerColor,
            trackColor = BgDark,
        )
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    answerRevealed: Boolean,
    onAnswer: (String) -> Unit,
) {
    val prompt = when (question) {
        is QuizQuestion.GuessMovie -> "Which movie is this?"
        is QuizQuestion.GuessYear -> "What year was this released?"
        is QuizQuestion.GuessActor -> "Who is the lead actor?"
    }
    val subtitle = when (question) {
        is QuizQuestion.GuessMovie -> null
        is QuizQuestion.GuessYear -> question.title
        is QuizQuestion.GuessActor -> question.title
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // movie image
        AsyncImage(
            model = question.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = prompt,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = TextMuted)
        }

        Spacer(Modifier.height(16.dp))

        // answer options
        val options = question.optionStrings()
        val correct = question.correctAnswerString()

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val btnColor = when {
                    !answerRevealed -> NeutralBtn
                    option == correct -> CorrectGreen
                    option == selectedAnswer -> WrongRed
                    else -> NeutralBtn
                }
                Button(
                    onClick = { if (!answerRevealed) onAnswer(option) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = option,
                        color = TextPrimary,
                        fontWeight = if (answerRevealed && option == correct) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizStartScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Quiz,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(72.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Movie Quiz",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "10 questions · 60 seconds",
            fontSize = 14.sp,
            color = TextMuted,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Guess the movie, year, or lead actor from a screenshot. Answer quickly — time is part of your score.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color.Black,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Start Quiz", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
