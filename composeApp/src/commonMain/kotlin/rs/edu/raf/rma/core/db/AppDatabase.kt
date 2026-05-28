package rs.edu.raf.rma.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.edu.raf.rma.db.ActorEntity
import rs.edu.raf.rma.db.FavoriteEntity
import rs.edu.raf.rma.db.GenreEntity
import rs.edu.raf.rma.db.MovieActorCrossRef
import rs.edu.raf.rma.db.MovieDao
import rs.edu.raf.rma.db.MovieEntity
import rs.edu.raf.rma.db.MovieGenreCrossRef
import rs.edu.raf.rma.db.QuizSessionEntity
import rs.edu.raf.rma.db.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        GenreEntity::class,
        MovieGenreCrossRef::class,
        ActorEntity::class,
        MovieActorCrossRef::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizSessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
