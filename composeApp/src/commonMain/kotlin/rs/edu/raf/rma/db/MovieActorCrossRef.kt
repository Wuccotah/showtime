package rs.edu.raf.rma.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_actors",
    primaryKeys = ["movieId", "actorId"],
    indices = [Index("actorId")],
)
data class MovieActorCrossRef(
    val movieId: String,
    val actorId: String,
)
