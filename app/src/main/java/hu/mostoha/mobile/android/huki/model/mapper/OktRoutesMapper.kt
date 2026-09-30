package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktStampTag
import hu.mostoha.mobile.android.huki.model.domain.OktStampWaypoint
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.OktDistanceFromMeUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesSavedStateUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.selectedRoute
import hu.mostoha.mobile.android.huki.model.ui.OktStampUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.formatter.TravelTimeFormatter
import hu.mostoha.mobile.android.huki.util.calculateLegDistances
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.util.trackBetween
import hu.mostoha.mobile.android.huki.util.distanceBetween
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import org.osmdroid.util.GeoPoint
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
                    stamps = if (isFullRoute) {
                        emptyList()
                    } else {
                        mapStamps(oktRouteGeometry.locations, oktRouteGeometry.stampWaypoints)
                    },
                    reversedStamps = if (isFullRoute) {
                        emptyList()
                    } else {
                        mapStamps(
                            oktRouteGeometry.locations.asReversed(),
                            oktRouteGeometry.stampWaypoints.asReversed()
                        )
                    },
                    distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                    inclineText = DistanceFormatter.format(oktRoute.incline),
                    declineText = DistanceFormatter.format(oktRoute.decline),
                    travelTimeText = TravelTimeFormatter.formatHoursAndMinutes(oktRoute.travelTime).toMessage(),
                    detailsUrl = if (isFullRoute) {
                        oktType.baseUrl
                    } else {
                        oktType.sectionTemplateUrl.format(oktRoute.id.lowercase())
                    },
                    isSelected = isFullRoute,
                    isReversed = false,
                    isStarted = false,
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
            .sortedWith(compareBy({ it.stampTag.stampNumber }, { it.stampTag.stampTag }))
    }

    fun mapDistanceFromMe(track: List<Location>, myLocation: Location, target: GeoPoint): OktDistanceFromMeUiModel {
        val isOnTrack = track.any { it.distanceBetween(myLocation) <= ON_TRACK_THRESHOLD_METERS }
        if (!isOnTrack) {
            return OktDistanceFromMeUiModel(geoPoint = target, distanceText = null, travelTimeText = null)
        }

        val trackToTarget = track.trackBetween(myLocation, target.toLocation())

        return OktDistanceFromMeUiModel(
            geoPoint = target,
            distanceText = DistanceFormatter.format(trackToTarget.calculateDistance()),
            travelTimeText = TravelTimeFormatter.format(trackToTarget.calculateTravelTime()),
        )
    }

    fun mapSavedState(oktRoutes: OktRoutesUiModel): OktRoutesSavedStateUiModel {
        return OktRoutesSavedStateUiModel(
            oktType = oktRoutes.oktType,
            selectedOktId = oktRoutes.selectedRoute.oktId,
            isStarted = oktRoutes.selectedRoute.isStarted,
            isReversed = oktRoutes.selectedRoute.isReversed,
        )
    }

    fun applySavedState(oktRoutes: OktRoutesUiModel, savedState: OktRoutesSavedStateUiModel): OktRoutesUiModel {
        if (oktRoutes.routes.none { it.oktId == savedState.selectedOktId }) return oktRoutes

        return oktRoutes.copy(
            routes = oktRoutes.routes.map { route ->
                val isSelected = route.oktId == savedState.selectedOktId
                route.copy(
                    isSelected = isSelected,
                    isStarted = isSelected && savedState.isStarted,
                    isReversed = isSelected && savedState.isReversed,
                )
            }
        )
    }

    private fun mapStamps(locations: List<Location>, stampWaypoints: List<OktStampWaypoint>): List<OktStampUiModel> {
        val legDistances = locations.calculateLegDistances(stampWaypoints.map { it.location })

        return stampWaypoints.zip(legDistances) { stamp, legDistance ->
            stamp.toOktStampUiModel(legDistance)
        }
    }

    private fun OktStampWaypoint.toOktStampUiModel(legDistance: Int): OktStampUiModel {
        return OktStampUiModel(
            stampTag = stampTag.stampTag,
            title = title,
            description = description,
            geoPoint = location.toGeoPoint(),
            distanceText = DistanceFormatter.formatRelative(legDistance),
        )
    }

    private fun mapStampTag(oktType: OktType, description: String): OktStampTag {
        val regex = """${oktType.stampTag}_(\d+)(?:_(\d+))?""".toRegex()
        val matchResult = regex.find(description)

        checkNotNull(matchResult) {
            "Stamp number not found in description: $description"
        }

        val stampTag = STAMP_TAG_REGEX.findAll(description)
            .map { it.value }
            .last { it.contains(matchResult.value) }
        val number1 = matchResult.groups[1]!!.value
        val number2 = matchResult.groups[2]?.value

        val stampNumber = if (number2.isNullOrEmpty()) {
            number1
        } else {
            "$number1.$number2"
        }

        return OktStampTag(stampTag, stampNumber.toDouble())
    }

    companion object {
        private const val ON_TRACK_THRESHOLD_METERS = 500
        private val STAMP_TAG_REGEX = """[A-Z]+PH(?:_[A-Z0-9]+)+""".toRegex()
    }

}
