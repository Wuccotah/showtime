package rs.edu.raf.rma.quiz

import rs.edu.raf.rma.db.MovieDao
import rs.edu.raf.rma.db.MovieEntity

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
private const val SESSION_SIZE = 10
private const val MAX_TYPE_COUNT = 4
private const val MIN_MOVIES = 10

private enum class QuestionType { GUESS_MOVIE, GUESS_YEAR, GUESS_ACTOR }

class QuizGenerator(private val dao: MovieDao) {

    suspend fun canStart(): Boolean = dao.countMoviesWithImages() >= MIN_MOVIES

    suspend fun generate(): List<QuizQuestion> {
        val moviesWithImages = dao.getMoviesWithImages()
        val moviesWithPosters = dao.getMoviesWithPosters()

        if (moviesWithImages.size < MIN_MOVIES) return emptyList()

        val allTitles = moviesWithImages.map { it.title }
        val allActors = dao.getAllActors().map { it.name }

        val types = buildTypeList().shuffled().take(SESSION_SIZE)
        val usedImages = mutableSetOf<String>()
        val questions = mutableListOf<QuizQuestion>()

        for (type in types) {
            val question = when (type) {
                QuestionType.GUESS_MOVIE ->
                    tryGuessMovie(moviesWithImages, usedImages, allTitles)
                QuestionType.GUESS_YEAR ->
                    tryGuessYear(moviesWithPosters, usedImages)
                QuestionType.GUESS_ACTOR ->
                    tryGuessActor(moviesWithPosters, usedImages, allActors)
            }
            if (question != null) questions.add(question)
        }

        return questions
    }

    private fun buildTypeList(): List<QuestionType> =
        List(MAX_TYPE_COUNT) { QuestionType.GUESS_MOVIE } +
        List(MAX_TYPE_COUNT) { QuestionType.GUESS_YEAR } +
        List(MAX_TYPE_COUNT) { QuestionType.GUESS_ACTOR }

    private suspend fun tryGuessMovie(
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
        allTitles: List<String>,
    ): QuizQuestion.GuessMovie? {
        for (movie in pool.shuffled()) {
            val imageUrl = pickUnusedImage(movie, usedImages) ?: continue
            val wrongTitles = allTitles
                .filter { it != movie.title }
                .shuffled()
                .take(3)
            if (wrongTitles.size < 3) continue
            usedImages.add(imageUrl)
            return QuizQuestion.GuessMovie(
                movieId = movie.imdbId,
                imageUrl = imageUrl,
                correctTitle = movie.title,
                options = (wrongTitles + movie.title).shuffled(),
            )
        }
        return null
    }

    private suspend fun tryGuessYear(
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
    ): QuizQuestion.GuessYear? {
        for (movie in pool.shuffled()) {
            val year = movie.year ?: continue
            val posterUrl = posterUrl(movie.posterPath) ?: continue
            if (posterUrl in usedImages) continue
            val wrongYears = generateWrongYears(year)
            if (wrongYears.size < 3) continue
            usedImages.add(posterUrl)
            return QuizQuestion.GuessYear(
                movieId = movie.imdbId,
                imageUrl = posterUrl,
                title = movie.title,
                correctYear = year,
                options = (wrongYears + year).shuffled(),
            )
        }
        return null
    }

    private suspend fun tryGuessActor(
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
        allActors: List<String>,
    ): QuizQuestion.GuessActor? {
        for (movie in pool.shuffled()) {
            val posterUrl = posterUrl(movie.posterPath) ?: continue
            if (posterUrl in usedImages) continue
            val withActors = dao.getMovieWithActors(movie.imdbId) ?: continue
            val cast = withActors.actors.take(3).map { it.name }
            if (cast.isEmpty()) continue
            val correctActor = cast.random()
            val wrongActors = allActors
                .filter { it !in cast }
                .shuffled()
                .take(3)
            if (wrongActors.size < 3) continue
            usedImages.add(posterUrl)
            return QuizQuestion.GuessActor(
                movieId = movie.imdbId,
                imageUrl = posterUrl,
                title = movie.title,
                correctActor = correctActor,
                options = (wrongActors + correctActor).shuffled(),
            )
        }
        return null
    }

    private fun pickUnusedImage(movie: MovieEntity, usedImages: Set<String>): String? {
        val backdrops = movie.backdropPaths
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.map { "${IMAGE_BASE_URL}w780$it" }
            ?: emptyList()
        val poster = posterUrl(movie.posterPath)
        val candidates = (backdrops + listOfNotNull(poster)).filter { it !in usedImages }
        return candidates.randomOrNull()
    }

    private fun posterUrl(path: String?): String? =
        if (path.isNullOrBlank()) null else "${IMAGE_BASE_URL}w342$path"

    private fun generateWrongYears(correct: Int): List<Int> {
        val offsets = (1..10).toMutableList().shuffled()
        val result = mutableListOf<Int>()
        val used = mutableSetOf(correct)
        for (offset in offsets) {
            val candidate = if (result.size % 2 == 0) correct - offset else correct + offset
            if (candidate > 0 && candidate !in used) {
                result.add(candidate)
                used.add(candidate)
            }
            if (result.size == 3) break
        }
        return result
    }
}
