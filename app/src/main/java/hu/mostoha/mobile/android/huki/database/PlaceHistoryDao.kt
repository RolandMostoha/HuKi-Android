package hu.mostoha.mobile.android.huki.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import hu.mostoha.mobile.android.huki.model.db.PlaceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Suppress("MaxLineLength")
@Dao
interface PlaceHistoryDao {

    @Query("SELECT * FROM place_history")
    fun getEntities(): Flow<List<PlaceHistoryEntity>>

    @Query("SELECT * FROM place_history WHERE (lower(name) GLOB '*' || :searchText || '*' OR lower(address) GLOB '*' || :searchText || '*' OR lower(comment) GLOB '*' || :searchText || '*') LIMIT :limit ")
    fun getEntities(searchText: String, limit: Int = -1): Flow<List<PlaceHistoryEntity>>

    @Query("SELECT * FROM place_history WHERE osm_id = :osmId")
    fun getEntity(osmId: String): Flow<PlaceHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg places: PlaceHistoryEntity)

    @Query("UPDATE place_history SET is_favourite = :isFavorite WHERE osm_id = :osmId")
    fun updateFavorite(osmId: String, isFavorite: Boolean)

    @Query("UPDATE place_history SET comment = :comment WHERE osm_id = :osmId")
    fun updateComment(osmId: String, comment: String)

    @Query("DELETE FROM place_history WHERE osm_id = :osmId")
    fun delete(osmId: String)

    @RawQuery
    fun deleteAll(query: SupportSQLiteQuery): Int

    fun deleteAll() {
        val query = SimpleSQLiteQuery("DELETE FROM ${PlaceHistoryEntity.TABLE_NAME}")
        deleteAll(query)
    }

    @Query("SELECT COUNT(osm_id) FROM place_history")
    fun getRowCount(): Int

    @Query("DELETE FROM place_history WHERE osm_id IN (SELECT osm_id FROM place_history ORDER BY last_modified LIMIT :count)")
    fun deleteLastRows(count: Int)

}
