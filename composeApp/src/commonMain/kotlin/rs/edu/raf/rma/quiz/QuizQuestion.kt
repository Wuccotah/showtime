package rs.edu.raf.rma.quiz

sealed class QuizQuestion {
    abstract val movieId: String
    abstract val imageUrl: String

    data class GuessMovie(
        override val movieId: String,
        override val imageUrl: String,
        val correctTitle: String,
        val options: List<String>,
    ) : QuizQuestion()

    data class GuessYear(
        override val movieId: String,
        override val imageUrl: String,
        val title: String,
        val correctYear: Int,
        val options: List<Int>,
    ) : QuizQuestion()

    data class GuessActor(
        override val movieId: String,
        override val imageUrl: String,
        val title: String,
        val correctActor: String,
        val options: List<String>,
    ) : QuizQuestion()
}

fun QuizQuestion.correctAnswerString(): String = when (this) {
    is QuizQuestion.GuessMovie -> correctTitle
    is QuizQuestion.GuessYear -> correctYear.toString()
    is QuizQuestion.GuessActor -> correctActor
}

fun QuizQuestion.optionStrings(): List<String> = when (this) {
    is QuizQuestion.GuessMovie -> options
    is QuizQuestion.GuessYear -> options.map { it.toString() }
    is QuizQuestion.GuessActor -> options
}
