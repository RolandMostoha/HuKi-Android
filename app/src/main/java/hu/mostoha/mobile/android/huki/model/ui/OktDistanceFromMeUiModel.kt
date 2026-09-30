package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

data class OktDistanceFromMeUiModel(
    val geoPoint: GeoPoint,
    val distanceText: Message.Res?,
    val travelTimeText: Message.Res?,
)
