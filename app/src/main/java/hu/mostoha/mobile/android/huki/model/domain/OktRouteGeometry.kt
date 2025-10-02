package hu.mostoha.mobile.android.huki.model.domain

data class OktRouteGeometry(
    val oktRoute: OktRoute,
    val locations: List<Location>,
    val stampWaypoints: List<OktStampWaypoint>,
)
