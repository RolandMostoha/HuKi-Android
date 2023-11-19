package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.HistoryInfo
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import org.osmdroid.util.GeoPoint

data class PlaceUiModel(
    val osmId: String,
    val placeType: PlaceType,
    val geoPoint: GeoPoint,
    val primaryText: Message,
    val secondaryText: Message,
    val placeFeature: PlaceFeature,
    @DrawableRes val iconRes: Int,
    val historyInfo: HistoryInfo? = null,
    val boundingBox: BoundingBox? = null,
    val distanceText: Message? = null
)
