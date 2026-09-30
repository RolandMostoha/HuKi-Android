package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

data class OktStampUiModel(
    val stampTag: String,
    val title: String,
    val description: String,
    val geoPoint: GeoPoint,
    val distanceText: Message.Res,
)
