package rs.edu.raf.rma.quiz

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF14181C)
private val BgCard = Color(0xFF22272E)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val AccentGreen = Color(0xFF00E054)
private val AccentYellow = Color(0xFFF5C518)
private val CorrectGreen = Color(0xFF2ECC71)
private val WrongRed = Color(0xFFE74C3C)

@Composable
fun QuizResultScreen(
    score: Float,
    correctCount: Int,
    totalQuestions: Int,
    timeUsedSeconds: Int,
    onDone: () -> Unit,
) {
    val incorrectCount = totalQuestions - correctCount
    val scoreColor = when {
        score >= 80 -> AccentGreen
        score >= 50 -> AccentYellow
        else -> WrongRed
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "QUIZ COMPLETE",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "%.1f".format(score),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = scoreColor,
            )

            Text(
                text = "/ 100",
                fontSize = 18.sp,
                color = TextMuted,
            )

            Spacer(Modifier.height(32.dp))

            // stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Correct",
                    value = correctCount.toString(),
                    valueColor = CorrectGreen,
                )
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Wrong",
                    value = incorrectCount.toString(),
                    valueColor = WrongRed,
                )
                ResultStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Time",
                    value = "${timeUsedSeconds}s",
                    valueColor = TextPrimary,
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = Color.Black,
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Done", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ResultStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
) {
    Column(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = TextMuted)
    }
}
