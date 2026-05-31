package rs.edu.raf.rma.movies.data

import rs.edu.raf.rma.db.ActorEntity
import rs.edu.raf.rma.db.GenreEntity
import rs.edu.raf.rma.db.MovieActorCrossRef
import rs.edu.raf.rma.db.MovieEntity
import rs.edu.raf.rma.db.MovieGenreCrossRef
import rs.edu.raf.rma.db.MovieWithGenres
import rs.edu.raf.rma.db.MovieWithGenresAndActors
import rs.edu.raf.rma.demo.Genre
import rs.edu.raf.rma.demo.MovieItem
import rs.edu.raf.rma.networking.model.MovieListItem
import rs.edu.raf.rma.networking.model.PersonSummary

fun MovieListItem.toEntity() = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
)

fun MovieItem.toEntity() = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    tmdbId = tmdbId,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    releaseDate = releaseDate,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    languageCode = languageCode,
    popularity = popularity,
    tmdbRating = tmdbRating,
    tmdbVotes = tmdbVotes,
    backdropPath = backdropPath,
    homepage = homepage,
    directorName = directorName,
    trailerUrl = trailerUrl,
)

fun Genre.toEntity() = GenreEntity(id = id, name = name)

fun GenreEntity.toDomain() = Genre(id = id, name = name)

fun PersonSummary.toEntity() = ActorEntity(
    imdbId = imdbId,
    name = name,
    profilePath = profilePath,
)

fun ActorEntity.toDomain() = PersonSummary(
    imdbId = imdbId,
    name = name,
    profilePath = profilePath,
)

fun MovieWithGenres.toDomain() = MovieItem(
    imdbId = movie.imdbId,
    title = movie.title,
    year = movie.year,
    imdbRating = movie.imdbRating,
    imdbVotes = movie.imdbVotes,
    posterPath = movie.posterPath,
    tmdbId = movie.tmdbId,
    originalTitle = movie.originalTitle,
    overview = movie.overview,
    tagline = movie.tagline,
    releaseDate = movie.releaseDate,
    runtime = movie.runtime,
    budget = movie.budget,
    revenue = movie.revenue,
    languageCode = movie.languageCode,
    popularity = movie.popularity,
    tmdbRating = movie.tmdbRating,
    tmdbVotes = movie.tmdbVotes,
    backdropPath = movie.backdropPath,
    homepage = movie.homepage,
    genres = genres.map { it.toDomain() },
    directorName = movie.directorName,
    trailerUrl = movie.trailerUrl,
)

fun MovieWithGenresAndActors.toDomain() = MovieItem(
    imdbId = movie.imdbId,
    title = movie.title,
    year = movie.year,
    imdbRating = movie.imdbRating,
    imdbVotes = movie.imdbVotes,
    posterPath = movie.posterPath,
    tmdbId = movie.tmdbId,
    originalTitle = movie.originalTitle,
    overview = movie.overview,
    tagline = movie.tagline,
    releaseDate = movie.releaseDate,
    runtime = movie.runtime,
    budget = movie.budget,
    revenue = movie.revenue,
    languageCode = movie.languageCode,
    popularity = movie.popularity,
    tmdbRating = movie.tmdbRating,
    tmdbVotes = movie.tmdbVotes,
    backdropPath = movie.backdropPath,
    homepage = movie.homepage,
    genres = genres.map { it.toDomain() },
    directorName = movie.directorName,
    trailerUrl = movie.trailerUrl,
)

fun MovieListItem.toGenreEntities(): List<GenreEntity> =
    genres.map { it.toEntity() }

fun MovieListItem.toGenreLinks(): List<MovieGenreCrossRef> =
    genres.map { MovieGenreCrossRef(movieId = imdbId, genreId = it.id) }

fun MovieItem.toGenreEntities(): List<GenreEntity> =
    genres.map { it.toEntity() }

fun MovieItem.toGenreLinks(): List<MovieGenreCrossRef> =
    genres.map { MovieGenreCrossRef(movieId = imdbId, genreId = it.id) }

fun MovieItem.toActorLinks(actors: List<ActorEntity>): List<MovieActorCrossRef> =
    actors.map { MovieActorCrossRef(movieId = imdbId, actorId = it.imdbId) }
