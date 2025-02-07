package hu.mostoha.mobile.android.huki.model.mapper

import androidx.core.util.toRange
import hu.mostoha.mobile.android.huki.data.AKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.data.RPDDK_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktStampTag
import hu.mostoha.mobile.android.huki.model.domain.OktStampWaypoint
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.util.KEKTURA_AKT_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_AKT_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.KEKTURA_RPDDK_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_RPDDK_URL_TEMPLATE
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import javax.inject.Inject

class OktRoutesMapper @Inject constructor() {

    fun map(oktType: OktType, oktRoutes: OktRoutes, oktRouteList: List<OktRoute>): OktRoutesUiModel {
        val oktFullGeoPoints = oktRoutes.locations.toGeoPoints()

        return OktRoutesUiModel(
            oktType = oktType,
            mapGeoPoints = oktFullGeoPoints,
            routes = oktRouteList.mapNotNull { oktRoute ->
                val startGeoPoint = oktRoute.start.toGeoPoint()
                val endGeoPoint = oktRoute.end.toGeoPoint()
                val isFullRoute = when (oktType) {
                    OktType.OKT -> oktRoute.id == OKT_ID_FULL_ROUTE
                    OktType.RPDDK -> oktRoute.id == RPDDK_ID_FULL_ROUTE
                    OktType.AKT -> oktRoute.id == AKT_ID_FULL_ROUTE
                }

                check(oktFullGeoPoints.contains(startGeoPoint) && oktFullGeoPoints.contains(endGeoPoint)) {
                    "${oktRoute.id}: start or end point not found in full route"

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
                    oktRoutes.stampWaypoints.filter { it.stampTag.stampNumber in oktRoute.stampTagsRange.toRange() }
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
                        when (oktType) {
                            OktType.OKT -> KEKTURA_OKT_URL
                            OktType.RPDDK -> KEKTURA_RPDDK_URL
                            OktType.AKT -> KEKTURA_AKT_URL
                        }
                    } else {
                        when (oktType) {
                            OktType.OKT -> KEKTURA_OKT_URL_TEMPLATE.format(oktRoute.id.lowercase())
                            OktType.RPDDK -> KEKTURA_RPDDK_URL_TEMPLATE.format(oktRoute.id.lowercase())
                            OktType.AKT -> KEKTURA_AKT_URL_TEMPLATE.format(oktRoute.id.lowercase())
                        }
                    },
                    isSelected = isFullRoute,
                )
            },
        )
    }

    fun map(oktType: OktType, gpxWaypoints: List<WayPoint>): List<OktStampWaypoint> {
        return gpxWaypoints
            .map { waypoint ->
                OktStampWaypoint(
                    title = waypoint.name!!,
                    description = waypoint.desc!!,
                    location = Location(waypoint.latitude, waypoint.longitude, waypoint.elevation),
                    stampTag = mapStampTag(oktType, waypoint.desc!!),
                )
            }
            .sortedBy { it.stampTag.stampNumber }
    }

    private fun mapStampTag(oktType: OktType, description: String): OktStampTag {
        val regex = """${oktType.stampTag}_(\d+)(?:_(\d+))?""".toRegex()
        val matchResult = regex.find(description)

        checkNotNull(matchResult) {
            "Stamp number not found in description: $description"
        }

        val stampTag = matchResult.groups[0]!!.value
        val number1 = matchResult.groups[1]!!.value
        val number2 = matchResult.groups[2]?.value

        val stampNumber = if (number2.isNullOrEmpty()) {
            number1
        } else {
            "$number1.$number2"
        }

        return OktStampTag(stampTag, stampNumber.toDouble())
    }

}
