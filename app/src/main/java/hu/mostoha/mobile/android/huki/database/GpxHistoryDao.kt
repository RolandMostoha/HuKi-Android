package hu.mostoha.mobile.android.huki.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import hu.mostoha.mobile.android.huki.model.db.GpxHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxHistoryDao {

    @Query("SELECT * FROM gpx_history ORDER BY last_opened DESC")
    fun getEntities(): Flow<List<GpxHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg places: GpxHistoryEntity)

    @Query("DELETE FROM gpx_history WHERE file_path = :filePath")
    fun delete(filePath: String)

    @RawQuery
    fun deleteAll(query: SupportSQLiteQuery): Int

    fun deleteAll() {
        val query = SimpleSQLiteQuery("DELETE FROM ${GpxHistoryEntity.TABLE_NAME}")
        deleteAll(query)
    }

}
