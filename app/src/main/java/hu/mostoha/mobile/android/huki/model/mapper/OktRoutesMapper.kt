package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.constants.KEKTURA_ROUTE_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.constants.KEKTURA_URL
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class OktRoutesMapper @Inject constructor() {

    fun map(oktFullGeoPoints: List<GeoPoint>, oktRoutes: List<OktRoute>): OktRoutesUiModel {
        return OktRoutesUiModel(
            mapGeoPoints = oktFullGeoPoints,
            routes = oktRoutes.mapNotNull { oktRoute ->
                val startGeoPoint = oktRoute.start.toGeoPoint()
                val endGeoPoint = oktRoute.end.toGeoPoint()
                val isFullRoute = oktRoute.id == OKT_ID_FULL_ROUTE

                if (!oktFullGeoPoints.contains(startGeoPoint) || !oktFullGeoPoints.contains(endGeoPoint)) {
                    return@mapNotNull null
                }

                val routeGeoPoints = if (isFullRoute) {
                    oktFullGeoPoints
                } else {
                    val fromIndex = oktFullGeoPoints.indexOf(startGeoPoint)
                    val toIndex = oktFullGeoPoints.indexOf(endGeoPoint)

                    oktFullGeoPoints.subList(fromIndex, toIndex + 1)
                }

                OktRouteUiModel(
                    id = oktRoute.id,
                    routeNumber = oktRoute.id.split("-").getOrElse(1) { "" },
                    routeName = oktRoute.name,
                    geoPoints = routeGeoPoints,
                    start = startGeoPoint,
                    end = endGeoPoint,
                    distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                    inclineText = DistanceFormatter.formatSigned(oktRoute.incline),
                    declineText = DistanceFormatter.formatSigned(-1 * oktRoute.decline),
                    travelTimeText = oktRoute.travelTime.formatHoursAndMinutes().toMessage(),
                    detailsUrl = if (isFullRoute) {
                        KEKTURA_URL
                    } else {
                        KEKTURA_ROUTE_URL_TEMPLATE.format(oktRoute.id.lowercase())
                    },
                    isSelected = isFullRoute,
                )
            },
        )
    }

}
