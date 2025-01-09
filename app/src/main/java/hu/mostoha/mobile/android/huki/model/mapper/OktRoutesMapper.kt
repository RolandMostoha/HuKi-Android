package hu.mostoha.mobile.android.huki.model.mapper

import androidx.core.util.toRange
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktStampWaypoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.util.KEKTURA_ROUTE_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.KEKTURA_URL
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import javax.inject.Inject

class OktRoutesMapper @Inject constructor() {

    fun map(oktRoutes: OktRoutes, oktRouteList: List<OktRoute>): OktRoutesUiModel {
        val oktFullGeoPoints = oktRoutes.locations.toGeoPoints()

        return OktRoutesUiModel(
            mapGeoPoints = oktFullGeoPoints,
            routes = oktRouteList.mapNotNull { oktRoute ->
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

                val stampWaypoints = if (isFullRoute) {
                    emptyList()
                } else {
                    oktRoutes.stampWaypoints.filter { it.stampNumber in oktRoute.stampTagsRange.toRange() }
                }

                OktRouteUiModel(
                    oktId = oktRoute.id,
                    routeNumber = oktRoute.id
                        .split("-")
                        .getOrElse(1) { "" },
                    routeName = oktRoute.name,
                    geoPoints = routeGeoPoints,
                    start = startGeoPoint,
                    end = endGeoPoint,
                    stampWaypoints = stampWaypoints,
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

    fun map(gpxWaypoints: List<WayPoint>): List<OktStampWaypoint> {
        return gpxWaypoints
            .map { waypoint ->
                OktStampWaypoint(
                    title = waypoint.name!!,
                    description = waypoint.desc!!,
                    location = Location(waypoint.latitude, waypoint.longitude, waypoint.elevation),
                    stampTag = mapStampTag(waypoint.desc!!),
                    stampNumber = mapStampNumber(waypoint.desc!!),
                )
            }
            .sortedBy { it.stampNumber }
    }

    private fun mapStampTag(description: String): String {
        val regex = "\\(OKTPH.*\\)".toRegex()
        val matchResult = regex.find(description)

        checkNotNull(matchResult) {
            "Stamp tag not found in description: $description"
        }

        return matchResult.value
    }

    private fun mapStampNumber(description: String): Double {
        val regex = """OKTPH_(\d+)(?:_(\d+))?""".toRegex()
        val matchResult = regex.find(description)

        checkNotNull(matchResult) {
            "Stamp number not found in description: $description"
        }

        val number1 = matchResult.groups[1]!!.value
        val number2 = matchResult.groups[2]?.value

        val stampNumber = if (number2.isNullOrEmpty()) {
            number1
        } else {
            "$number1.$number2"
        }

        return stampNumber.toDouble()
    }

}
