package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location

data class PlaceArea(
    val placeAreaType: PlaceAreaType,
    val location: Location,
    val boundingBox: BoundingBox,
    val addressMessage: Message,
    val distanceMessage: Message,
    @DrawableRes val iconRes: Int,
)

enum class PlaceAreaType {
    MAP_SEARCH,
    PLACE_DETAILS,
    LANDSCAPE
}
