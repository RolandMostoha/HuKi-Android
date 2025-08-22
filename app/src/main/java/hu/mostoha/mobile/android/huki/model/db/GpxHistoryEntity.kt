package hu.mostoha.mobile.android.huki.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.mostoha.mobile.android.huki.model.domain.GpxType

@Entity(tableName = GpxHistoryEntity.TABLE_NAME)
data class GpxHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "last_opened")
    val lastOpened: Long,
    @ColumnInfo(name = "type")
    val type: GpxType,
) {
    companion object {
        const val TABLE_NAME = "gpx_history"
    }
}
