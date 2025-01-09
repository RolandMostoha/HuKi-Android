package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import org.osmdroid.util.GeoPoint

data class LandscapeUiModel(
    val osmId: String,
    val osmType: PlaceType,
    val name: Message,
    val geoPoint: GeoPoint,
    @DrawableRes val iconRes: Int
)
