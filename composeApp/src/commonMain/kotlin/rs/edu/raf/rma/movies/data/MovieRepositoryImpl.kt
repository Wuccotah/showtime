package rs.edu.raf.rma.movies.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.db.FavoriteEntity
import rs.edu.raf.rma.db.QuizSessionEntity
import rs.edu.raf.rma.db.WatchlistEntity
import rs.edu.raf.rma.demo.Genre
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

    override fun observeMovieBackdrops(imdbId: String): Flow<List<String>> =
        dao.observeMovieBackdrops(imdbId).map { raw ->
            raw?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        }

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
        val videos = runCatching { moviesApi.getVideos(imdbId) }.getOrNull()
        val images = runCatching { moviesApi.getImages(imdbId, type = "backdrop") }.getOrNull()

        val directorName = cast?.items
            ?.firstOrNull { it.department == "Directing" }
            ?.name

        val trailerUrl = videos
            ?.firstOrNull { it.site == "YouTube" && it.type == "Trailer" && it.key != null }
            ?.key
            ?.let { "https://www.youtube.com/watch?v=$it" }

        val backdropPaths = images?.backdrops
            ?.mapNotNull { it.filePath }
            ?.take(10)
            ?.joinToString(",")

        val actorPersons = cast?.items?.filter { it.department != "Directing" } ?: emptyList()
        val actors = actorPersons.map { it.toEntity() }

        val entity = movie.toEntity().copy(
            directorName = directorName,
            trailerUrl = trailerUrl,
            backdropPaths = backdropPaths,
        )

        dao.refreshMovieDetailTransaction(
            movie = entity,
            genres = movie.toGenreEntities(),
            genreLinks = movie.toGenreLinks(),
            actors = actors,
            actorLinks = movie.toActorLinks(actors),
        )
    }

    override suspend fun getGenres(): List<Genre> = moviesApi.getGenres()

    // favorites

    override fun observeFavorites(): Flow<List<MovieItem>> =
        dao.observeFavoriteMovies().map { list -> list.map { it.toDomain() } }

    override fun isFavorite(movieId: String): Flow<Boolean> =
        dao.isFavorite(movieId)

    override fun observeFavoritesCount(): Flow<Int> =
        dao.observeFavoritesCount()

    override suspend fun syncFavorites() {
        runCatching { moviesApi.getFavorites() }
            .onSuccess { items ->
                dao.replaceFavorites(items.map { it.imdbId })
            }
    }

    override suspend fun toggleFavorite(movieId: String, add: Boolean) {
        if (add) {
            dao.insertFavorite(FavoriteEntity(movieId))
            runCatching { moviesApi.addFavorite(movieId) }
                .onFailure { dao.deleteFavorite(movieId) }
        } else {
            dao.deleteFavorite(movieId)
            runCatching { moviesApi.removeFavorite(movieId) }
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
        runCatching { moviesApi.getWatchlist() }
            .onSuccess { items ->
                dao.replaceWatchlist(items.map { it.imdbId })
            }
    }

    override suspend fun toggleWatchlist(movieId: String, add: Boolean) {
        if (add) {
            dao.insertWatchlist(WatchlistEntity(movieId))
            runCatching { moviesApi.addToWatchlist(movieId) }
                .onFailure { dao.deleteWatchlist(movieId) }
        } else {
            dao.deleteWatchlist(movieId)
            runCatching { moviesApi.removeFromWatchlist(movieId) }
                .onFailure { dao.insertWatchlist(WatchlistEntity(movieId)) }
        }
    }

    // quiz

    override suspend fun countMoviesWithImages(): Int = dao.countMoviesWithImages()

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

    override suspend fun submitQuizResult(score: Float) {
        runCatching {
            moviesApi.submitQuizResult(rs.edu.raf.rma.networking.model.QuizSubmitBody(score = score))
        }
    }

    override fun observeBestScore(): Flow<Float?> = dao.observeBestScore()

    override fun observeQuizCount(): Flow<Int> = dao.observeQuizCount()

    // lifecycle

    override suspend fun clearUserData() {
        dao.deleteAllFavorites()
        dao.deleteAllWatchlist()
    }
}
