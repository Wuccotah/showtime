package rs.edu.raf.rma.movies.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.db.FavoriteEntity
import rs.edu.raf.rma.db.QuizSessionEntity
import rs.edu.raf.rma.db.WatchlistEntity
import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.model.PersonSummary

class MovieRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val moviesApi: MoviesApi,
) : MovieRepository {

    private val dao get() = appDatabase.movieDao()

    // catalog

    override fun observeMovies(): Flow<List<MovieItem>> =
        dao.observeAllMovies().map { list -> list.map { it.toDomain() } }

    override fun observeMovie(imdbId: String): Flow<MovieItem?> =
        dao.observeMovie(imdbId).map { it?.toDomain() }

    override fun observeMovieActors(imdbId: String): Flow<List<PersonSummary>> =
        dao.observeMovie(imdbId).map { it?.actors?.map { a -> a.toDomain() } ?: emptyList() }

    override suspend fun refreshMovies(
        query: String?,
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?,
        sortBy: String?,
        sortOrder: String?,
        page: Int,
    ) {
        val response = moviesApi.getMovies(
            query = query,
            page = page,
            genreId = genreId,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
        val movies = response.items.map { it.toEntity() }
        val genres = response.items.flatMap { it.toGenreEntities() }.distinctBy { it.id }
        val genreLinks = response.items.flatMap { it.toGenreLinks() }
        dao.refreshMovieListTransaction(movies, genres, genreLinks)
    }

    override suspend fun refreshMovieDetail(imdbId: String) {
        val movie = moviesApi.getMovie(imdbId)
        val cast = runCatching { moviesApi.getCast(imdbId) }.getOrNull()
        val actors = cast?.items?.map { it.toEntity() } ?: emptyList()
        dao.refreshMovieDetailTransaction(
            movie = movie.toEntity(),
            genres = movie.toGenreEntities(),
            genreLinks = movie.toGenreLinks(),
            actors = actors,
            actorLinks = movie.toActorLinks(actors),
        )
    }

    // favorites

    override fun observeFavorites(): Flow<List<MovieItem>> =
        dao.observeFavoriteMovies().map { list -> list.map { it.toDomain() } }

    override fun isFavorite(movieId: String): Flow<Boolean> =
        dao.isFavorite(movieId)

    override fun observeFavoritesCount(): Flow<Int> =
        dao.observeFavoritesCount()

    override suspend fun syncFavorites() {
        // TODO: implement when favorites API endpoint is available
    }

    override suspend fun toggleFavorite(movieId: String, add: Boolean) {
        if (add) {
            dao.insertFavorite(FavoriteEntity(movieId))
            runCatching { /* TODO: moviesApi.addFavorite(movieId) */ }
                .onFailure { dao.deleteFavorite(movieId) }
        } else {
            dao.deleteFavorite(movieId)
            runCatching { /* TODO: moviesApi.removeFavorite(movieId) */ }
                .onFailure { dao.insertFavorite(FavoriteEntity(movieId)) }
        }
    }

    // watchlist

    override fun observeWatchlist(): Flow<List<MovieItem>> =
        dao.observeWatchlistMovies().map { list -> list.map { it.toDomain() } }

    override fun isOnWatchlist(movieId: String): Flow<Boolean> =
        dao.isOnWatchlist(movieId)

    override fun observeWatchlistCount(): Flow<Int> =
        dao.observeWatchlistCount()

    override suspend fun syncWatchlist() {
        // TODO: implement when watchlist API endpoint is available
    }

    override suspend fun toggleWatchlist(movieId: String, add: Boolean) {
        if (add) {
            dao.insertWatchlist(WatchlistEntity(movieId))
            runCatching { /* TODO: moviesApi.addToWatchlist(movieId) */ }
                .onFailure { dao.deleteWatchlist(movieId) }
        } else {
            dao.deleteWatchlist(movieId)
            runCatching { /* TODO: moviesApi.removeFromWatchlist(movieId) */ }
                .onFailure { dao.insertWatchlist(WatchlistEntity(movieId)) }
        }
    }

    // quiz

    override suspend fun insertQuizSession(
        score: Float,
        correctAnswers: Int,
        totalQuestions: Int,
        timeUsedSeconds: Int,
        playedAt: Long,
    ) {
        dao.insertQuizSession(
            QuizSessionEntity(
                score = score,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                timeUsedSeconds = timeUsedSeconds,
                playedAt = playedAt,
            )
        )
    }

    override fun observeBestScore(): Flow<Float?> = dao.observeBestScore()

    override fun observeQuizCount(): Flow<Int> = dao.observeQuizCount()

    // lifecycle

    override suspend fun clearUserData() {
        dao.deleteAllFavorites()
        dao.deleteAllWatchlist()
    }
}
