package rs.edu.raf.rma.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    // movies

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsertMovie(movie: MovieEntity)

    @Transaction
    @Query("SELECT * FROM movies ORDER BY popularity DESC, imdbRating DESC")
    fun observeAllMovies(): Flow<List<MovieWithGenres>>

    @Transaction
    @Query("SELECT * FROM movies WHERE imdbId = :imdbId")
    fun observeMovie(imdbId: String): Flow<MovieWithGenresAndActors?>

    @Query("SELECT backdropPaths FROM movies WHERE imdbId = :imdbId")
    fun observeMovieBackdrops(imdbId: String): Flow<String?>

    // genres

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Query("DELETE FROM movie_genres WHERE movieId = :movieId")
    suspend fun deleteGenreLinksForMovie(movieId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenreLinks(links: List<MovieGenreCrossRef>)

    // actors

    @Upsert
    suspend fun upsertActors(actors: List<ActorEntity>)

    @Query("DELETE FROM movie_actors WHERE movieId = :movieId")
    suspend fun deleteActorLinksForMovie(movieId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActorLinks(links: List<MovieActorCrossRef>)

    @Query("""
        SELECT * FROM actors
        WHERE imdbId NOT IN (SELECT actorId FROM movie_actors WHERE movieId = :movieId)
        LIMIT :limit
    """)
    suspend fun getActorsNotInMovie(movieId: String, limit: Int = 20): List<ActorEntity>

    @Query("SELECT * FROM actors LIMIT :limit")
    suspend fun getAllActors(limit: Int = 200): List<ActorEntity>

    // quiz pool

    @Query("SELECT * FROM movies WHERE posterPath IS NOT NULL AND posterPath != '' ORDER BY RANDOM()")
    suspend fun getMoviesWithPosters(): List<MovieEntity>

    @Query("""
        SELECT * FROM movies
        WHERE (posterPath IS NOT NULL AND posterPath != '')
           OR (backdropPaths IS NOT NULL AND backdropPaths != '')
        ORDER BY RANDOM()
    """)
    suspend fun getMoviesWithImages(): List<MovieEntity>

    @Query("""
        SELECT COUNT(*) FROM movies
        WHERE (posterPath IS NOT NULL AND posterPath != '')
           OR (backdropPaths IS NOT NULL AND backdropPaths != '')
    """)
    suspend fun countMoviesWithImages(): Int

    @Transaction
    @Query("SELECT * FROM movies WHERE imdbId = :imdbId")
    suspend fun getMovieWithActors(imdbId: String): MovieWithGenresAndActors?

    // favorites

    @Upsert
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: String)

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    @Transaction
    @Query("SELECT movies.* FROM movies INNER JOIN favorites ON movies.imdbId = favorites.movieId")
    fun observeFavoriteMovies(): Flow<List<MovieWithGenres>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE movieId = :movieId)")
    fun isFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoritesCount(): Flow<Int>

    // watchlist

    @Upsert
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteWatchlist(movieId: String)

    @Query("DELETE FROM watchlist")
    suspend fun deleteAllWatchlist()

    @Transaction
    @Query("SELECT movies.* FROM movies INNER JOIN watchlist ON movies.imdbId = watchlist.movieId")
    fun observeWatchlistMovies(): Flow<List<MovieWithGenres>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE movieId = :movieId)")
    fun isOnWatchlist(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>

    // quiz

    @Insert
    suspend fun insertQuizSession(session: QuizSessionEntity)

    @Query("SELECT MAX(score) FROM quiz_sessions")
    fun observeBestScore(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    fun observeQuizCount(): Flow<Int>

    // transactions
    @Transaction
    suspend fun refreshMovieListTransaction(
        movies: List<MovieEntity>,
        genres: List<GenreEntity>,
        genreLinks: List<MovieGenreCrossRef>,
    ) {
        upsertMovies(movies)
        upsertGenres(genres)
        movies.forEach { deleteGenreLinksForMovie(it.imdbId) }
        insertGenreLinks(genreLinks)
    }

    @Transaction
    suspend fun refreshMovieDetailTransaction(
        movie: MovieEntity,
        genres: List<GenreEntity>,
        genreLinks: List<MovieGenreCrossRef>,
        actors: List<ActorEntity>,
        actorLinks: List<MovieActorCrossRef>,
    ) {
        upsertMovie(movie)
        upsertGenres(genres)
        deleteGenreLinksForMovie(movie.imdbId)
        insertGenreLinks(genreLinks)
        upsertActors(actors)
        deleteActorLinksForMovie(movie.imdbId)
        insertActorLinks(actorLinks)
    }

    @Transaction
    suspend fun replaceFavorites(movieIds: List<String>) {
        deleteAllFavorites()
        movieIds.forEach { insertFavorite(FavoriteEntity(it)) }
    }

    @Transaction
    suspend fun replaceWatchlist(movieIds: List<String>) {
        deleteAllWatchlist()
        movieIds.forEach { insertWatchlist(WatchlistEntity(it)) }
    }
}
