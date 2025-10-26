package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.Destination
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import org.osmdroid.util.GeoPoint

data class LandscapeUiModel(
    val osmId: String,
    val placeType: PlaceType,
    val name: Message.Res,
    val geoPoint: GeoPoint,
    @ColorRes val color: Int,
    @DrawableRes val iconRes: Int,
    val destinations: List<Destination>,
)
