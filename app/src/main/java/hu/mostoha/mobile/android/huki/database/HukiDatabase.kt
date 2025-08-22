package hu.mostoha.mobile.android.huki.database

import androidx.room.Database
import androidx.room.RoomDatabase
import hu.mostoha.mobile.android.huki.model.db.GpxHistoryEntity
import hu.mostoha.mobile.android.huki.model.db.PlaceHistoryEntity

@Database(
    entities = [
        PlaceHistoryEntity::class,
        GpxHistoryEntity::class,
    ],
    exportSchema = true,
    version = 2
)
abstract class HukiDatabase : RoomDatabase() {

    abstract fun placeHistoryDao(): PlaceHistoryDao

    abstract fun gpxHistoryDao(): GpxHistoryDao

}
