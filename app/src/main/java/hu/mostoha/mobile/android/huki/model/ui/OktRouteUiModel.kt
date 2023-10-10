package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

data class OktRouteUiModel(
    val oktId: String,
    val routeNumber: String,
    val routeName: String,
    val geoPoints: List<GeoPoint>,
    val start: GeoPoint,
    val end: GeoPoint,
    val distanceText: Message.Res,
    val inclineText: Message.Res,
    val declineText: Message.Res,
    val travelTimeText: Message.Text,
    val detailsUrl: String,
    val isSelected: Boolean,
)
