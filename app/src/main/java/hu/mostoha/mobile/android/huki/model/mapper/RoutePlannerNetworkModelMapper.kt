package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.extensions.nextInt
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.domain.toLocationsFromDoubles
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Algorithm
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Profile
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteRequest
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import hu.mostoha.mobile.android.huki.util.HIKE_CUSTOM_MODEL
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.util.isCloseWithThreshold
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.random.Random

class RoutePlannerNetworkModelMapper @Inject constructor() {

    companion object {
        private const val ROUND_TRIP_MAX_HEADING = 360
        private const val ROUND_TRIP_MAX_SEED = 100
    }

    fun createRouteRequest(planType: RoutePlanType, waypoints: List<Location>): RouteRequest {
        return when (planType) {
            is RoutePlanType.Hike -> {
                RouteRequest(
                    profile = Profile.HIKE,
                    pointsEncoded = false,
                    elevation = true,
                    instructions = false,
                    points = waypoints.map { location ->
                        listOf(location.longitude, location.latitude)
                    },
                    chDisabled = true,
                    customModel = HIKE_CUSTOM_MODEL,
                )
            }
            is RoutePlanType.Foot -> {
                RouteRequest(
                    profile = Profile.HIKE,
                    pointsEncoded = false,
                    elevation = true,
                    instructions = false,
                    points = waypoints.map { location ->
                        listOf(location.longitude, location.latitude)
                    },
                )
            }
            is RoutePlanType.Bike -> {
                RouteRequest(
                    profile = Profile.BIKE,
                    pointsEncoded = false,
                    elevation = true,
                    instructions = false,
                    points = waypoints.map { location ->
                        listOf(location.longitude, location.latitude)
                    },
                )
            }
            is RoutePlanType.RoundTrip -> {
                RouteRequest(
                    profile = Profile.HIKE,
                    pointsEncoded = false,
                    elevation = true,
                    instructions = false,
                    points = waypoints
                        .map { location ->
                            listOf(location.longitude, location.latitude)
                        }
                        .take(1),
                    algorithm = Algorithm.ROUND_TRIP,
                    roundTripDistance = planType.distanceM,
                    roundTripSeed = Random.nextInt(1..ROUND_TRIP_MAX_SEED),
                    heading = listOf(Random.nextInt(0..ROUND_TRIP_MAX_HEADING)),
                    headingPenalty = 1000,
                    chDisabled = true,
                    customModel = HIKE_CUSTOM_MODEL,
                )
            }
        }
    }

    fun mapRouteResponse(planType: RoutePlanType, routeResponse: RouteResponse): RoutePlan {
        val path = routeResponse.paths.first()
        val waypoints = path.snappedWaypoints.coordinates.toLocationsFromDoubles()
        val locations = path.points.coordinates.toLocationsFromDoubles()
        val minAltitude = locations
            .mapNotNull { it.altitude }
            .minOrNull() ?: 0.0
        val maxAltitude = locations
            .mapNotNull { it.altitude }
            .maxOrNull() ?: 0.0

        return RoutePlan(
            planType = planType,
            wayPoints = waypoints,
            locations = locations,
            travelTime = locations.calculateTravelTime(),
            distance = path.distance.roundToInt(),
            altitudeRange = minAltitude.toInt() to maxAltitude.toInt(),
            incline = path.ascend.roundToInt(),
            decline = path.descend.roundToInt(),
            isClosed = locations.first().isCloseWithThreshold(locations.last())
        )
    }

}
