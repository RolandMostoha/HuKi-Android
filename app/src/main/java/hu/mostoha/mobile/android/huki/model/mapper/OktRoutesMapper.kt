package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.Location
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
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import javax.inject.Inject

class OktRoutesMapper @Inject constructor() {

    fun map(oktType: OktType, oktRoutes: OktRoutes): OktRoutesUiModel {
        val oktFullGeoPoints = oktRoutes.locations.toGeoPoints()

        return OktRoutesUiModel(
            oktType = oktType,
            mapGeoPoints = oktFullGeoPoints,
            routes = oktRoutes.oktRoutes.map { oktRouteGeometry ->
                val oktRoute = oktRouteGeometry.oktRoute
                val isFullRoute = oktRoute.id == oktType.fullRouteId

                OktRouteUiModel(
                    oktId = oktRoute.id,
                    routeNumber = oktRoute.id
                        .split("-")
                        .getOrElse(1) { "" },
                    routeName = oktRoute.name,
                    geoPoints = oktRouteGeometry.locations.toGeoPoints(),
                    start = oktRoute.start.toGeoPoint(),
                    end = oktRoute.end.toGeoPoint(),
                    stampWaypoints = oktRouteGeometry.stampWaypoints,
                    distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                    inclineText = DistanceFormatter.formatSigned(oktRoute.incline),
                    declineText = DistanceFormatter.formatSigned(-1 * oktRoute.decline),
                    travelTimeText = oktRoute.travelTime.formatHoursAndMinutes().toMessage(),
                    detailsUrl = if (isFullRoute) {
                        oktType.baseUrl
                    } else {
                        oktType.sectionTemplateUrl.format(oktRoute.id.lowercase())
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
