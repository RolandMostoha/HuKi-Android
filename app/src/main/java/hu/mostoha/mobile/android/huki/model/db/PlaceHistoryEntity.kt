package hu.mostoha.mobile.android.huki.model.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType

@Entity(tableName = PlaceHistoryEntity.TABLE_NAME)
data class PlaceHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "osm_id")
    val osmId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "is_favourite")
    val isFavourite: Boolean,
    @ColumnInfo(name = "place_type")
    val placeType: PlaceType,
    @ColumnInfo(name = "place_feature")
    val placeFeature: PlaceFeature,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long,
    @ColumnInfo(name = "comment")
    val comment: String?,
    @Embedded(prefix = "bounding_box_")
    val boundingBox: BoundingBox?,
) {
    companion object {
        const val TABLE_NAME = "place_history"
    }
}
