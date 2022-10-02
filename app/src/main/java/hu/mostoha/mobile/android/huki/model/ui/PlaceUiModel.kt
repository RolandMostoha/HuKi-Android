package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.ui.util.Message
import org.osmdroid.util.GeoPoint

data class PlaceUiModel(
    val osmId: String,
    val placeType: PlaceType,
    val primaryText: Message,
    val secondaryText: Message?,
    @DrawableRes val iconRes: Int,
    val geoPoint: GeoPoint,
    val boundingBox: BoundingBox?,
    val isLandscape: Boolean,
    val distanceText: Message? = null
)
