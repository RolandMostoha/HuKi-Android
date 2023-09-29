package hu.mostoha.mobile.android.huki.model.ui

import org.osmdroid.util.GeoPoint

data class OktRoutesUiModel(
    val mapGeoPoints: List<GeoPoint>,
    val routes: List<OktRouteUiModel>,
)

val OktRoutesUiModel.selectedRoute: OktRouteUiModel
    get() = this.routes.first { it.isSelected }
