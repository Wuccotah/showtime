package rs.edu.raf.rma.movies.data

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.model.PersonSummary

interface MovieRepository {

    // catalog

    fun observeMovies(): Flow<List<MovieItem>>
    fun observeMovie(imdbId: String): Flow<MovieItem?>
    fun observeMovieActors(imdbId: String): Flow<List<PersonSummary>>

    suspend fun refreshMovies(
        query: String? = null,
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        page: Int = 1,
    )

    suspend fun refreshMovieDetail(imdbId: String)

    // favorites

    fun observeFavorites(): Flow<List<MovieItem>>
    fun isFavorite(movieId: String): Flow<Boolean>
    fun observeFavoritesCount(): Flow<Int>
    suspend fun syncFavorites()
    suspend fun toggleFavorite(movieId: String, add: Boolean)

    // watchlist

    fun observeWatchlist(): Flow<List<MovieItem>>
    fun isOnWatchlist(movieId: String): Flow<Boolean>
    fun observeWatchlistCount(): Flow<Int>
    suspend fun syncWatchlist()
    suspend fun toggleWatchlist(movieId: String, add: Boolean)

    // quiz

    suspend fun insertQuizSession(
        score: Float,
        correctAnswers: Int,
        totalQuestions: Int,
        timeUsedSeconds: Int,
        playedAt: Long,
    )
    fun observeBestScore(): Flow<Float?>
    fun observeQuizCount(): Flow<Int>

    // lifecycle

    suspend fun clearUserData()
}
