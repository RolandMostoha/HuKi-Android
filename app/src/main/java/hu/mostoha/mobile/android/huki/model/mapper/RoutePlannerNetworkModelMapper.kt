package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.toLocations
import hu.mostoha.mobile.android.huki.model.network.graphhopper.CustomModel
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Priority
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteRequest
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import hu.mostoha.mobile.android.huki.util.WAY_CLOSED_DISTANCE_THRESHOLD_METER
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.util.distanceBetween
import javax.inject.Inject
import kotlin.math.roundToInt

class RoutePlannerNetworkModelMapper @Inject constructor() {

    fun createRouteRequest(waypoints: List<Location>): RouteRequest {
        return RouteRequest(
            profile = "hike",
            pointsEncoded = false,
            elevation = true,
            instructions = false,
            points = waypoints.map { location ->
                listOf(location.longitude, location.latitude)
            },
            chDisabled = true,
            customModel = CustomModel(
                listOf(
                    Priority(
                        ifCondition = "foot_network == MISSING",
                        multiplyBy = "0.3"
                    )
                )
            ),
        )
    }

    fun mapRouteResponse(routeResponse: RouteResponse): RoutePlan {
        val path = routeResponse.paths.first()
        val waypoints = path.snappedWaypoints.coordinates.toLocations()
        val locations = path.points.coordinates.toLocations()
        val minAltitude = locations
            .mapNotNull { it.altitude }
            .minOrNull() ?: 0.0
        val maxAltitude = locations
            .mapNotNull { it.altitude }
            .maxOrNull() ?: 0.0

        return RoutePlan(
            wayPoints = waypoints,
            locations = locations,
            travelTime = locations.calculateTravelTime(),
            distance = path.distance.roundToInt(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = path.ascend.roundToInt(),
            decline = path.descend.roundToInt(),
            isClosed = locations.first()
                .distanceBetween(locations.last()) <= WAY_CLOSED_DISTANCE_THRESHOLD_METER,
        )
    }

}