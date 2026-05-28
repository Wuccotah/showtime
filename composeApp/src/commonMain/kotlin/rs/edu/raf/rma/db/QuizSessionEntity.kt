package rs.edu.raf.rma.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val timeUsedSeconds: Int,
    val playedAt: Long,
)
