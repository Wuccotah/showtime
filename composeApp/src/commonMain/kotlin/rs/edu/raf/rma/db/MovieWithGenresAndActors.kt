package rs.edu.raf.rma.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MovieWithGenresAndActors(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "imdbId",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieGenreCrossRef::class,
            parentColumn = "movieId",
            entityColumn = "genreId",
        ),
    )
    val genres: List<GenreEntity>,
    @Relation(
        parentColumn = "imdbId",
        entityColumn = "imdbId",
        associateBy = Junction(
            value = MovieActorCrossRef::class,
            parentColumn = "movieId",
            entityColumn = "actorId",
        ),
    )
    val actors: List<ActorEntity>,
)
