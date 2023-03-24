package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import org.osmdroid.util.GeoPoint

data class PlaceUiModel(
    val osmId: String,
    val placeType: PlaceType,
    val geoPoint: GeoPoint,
    val primaryText: Message,
    @DrawableRes val iconRes: Int,
    val isLandscape: Boolean = false,
    val boundingBox: BoundingBox? = null,
    val secondaryText: Message? = null,
    val distanceText: Message? = null
)
