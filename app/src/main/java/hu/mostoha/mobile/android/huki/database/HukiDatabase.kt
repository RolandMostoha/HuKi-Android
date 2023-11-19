package hu.mostoha.mobile.android.huki.database

import androidx.room.Database
import androidx.room.RoomDatabase
import hu.mostoha.mobile.android.huki.model.db.PlaceHistoryEntity

@Database(
    entities = [PlaceHistoryEntity::class],
    exportSchema = true,
    version = 1
)
abstract class HukiDatabase : RoomDatabase() {

    abstract fun placeHistoryDao(): PlaceHistoryDao

}
