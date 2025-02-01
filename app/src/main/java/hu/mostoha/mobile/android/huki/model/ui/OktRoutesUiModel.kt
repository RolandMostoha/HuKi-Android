package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.OktType
import org.osmdroid.util.GeoPoint

data class OktRoutesUiModel(
    val oktType: OktType,
    val mapGeoPoints: List<GeoPoint>,
    val routes: List<OktRouteUiModel>,
)

val OktRoutesUiModel.selectedRoute: OktRouteUiModel
    get() = this.routes.first { it.isSelected }
