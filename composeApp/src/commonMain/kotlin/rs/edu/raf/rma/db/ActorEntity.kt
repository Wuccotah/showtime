package rs.edu.raf.rma.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actors")
data class ActorEntity(
    @PrimaryKey val imdbId: String,
    val name: String,
    val profilePath: String? = null,
)
