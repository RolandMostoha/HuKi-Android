package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.network.graphhopper.CustomModel
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Hints
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Info
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Path
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Points
import hu.mostoha.mobile.android.huki.model.network.graphhopper.Priority
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteRequest
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import hu.mostoha.mobile.android.huki.model.network.graphhopper.SnappedWaypoints
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import org.junit.Test

class RoutePlannerNetworkModelMapperTest {

    private val mapper = RoutePlannerNetworkModelMapper()

    @Test
    fun `Given waypoints, when createRouteRequest, then route request returns`() {
        val waypoints = listOf(DEFAULT_WAYPOINT_1, DEFAULT_WAYPOINT_2)

        val routeRequest = mapper.createRouteRequest(waypoints)

        assertThat(routeRequest).isEqualTo(
            RouteRequest(
                profile = "hike",
                pointsEncoded = false,
                elevation = true,
                instructions = false,
                points = listOf(
                    listOf(DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE, DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE),
                    listOf(DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE, DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE),
                ),
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
        )
    }

    @Test
    fun `Given waypoints and route response, when createRomapRouteResponse, then route plan returns`() {
        val waypoints = listOf(
            listOf(
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE,
            ),
            listOf(
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE,
            )
        )
        val routeResponse = RouteResponse(
            hints = Hints(
                visitedNodesAverage = 20.0,
                visitedNodesSum = 30.0,
            ),
            info = Info(
                copyrights = emptyList(),
                took = 1
            ),
            paths = listOf(
                Path(
                    ascend = 500.0,
                    boundingBox = emptyList(),
                    descend = 400.0,
                    distance = 10000.0,
                    points = Points(
                        coordinates = waypoints,
                        type = "Point"
                    ),
                    pointsEncoded = false,
                    snappedWaypoints = SnappedWaypoints(
                        coordinates = waypoints,
                        type = "Point"
                    ),
                    time = 100,
                    transfers = 5,
                    weight = 0.5,
                )
            )
        )

        val routePlan = mapper.mapRouteResponse(routeResponse)

        val expectedLocations = listOf(DEFAULT_WAYPOINT_1, DEFAULT_WAYPOINT_2)

        assertThat(routePlan).isEqualTo(
            RoutePlan(
                id = routePlan.id,
                wayPoints = expectedLocations,
                locations = expectedLocations,
                travelTime = expectedLocations.calculateTravelTime(),
                distance = 10000,
                altitudeRange = Pair(461, 694),
                incline = 500,
                decline = 400,
                isClosed = false
            )
        )
    }

    companion object {
        val DEFAULT_WAYPOINT_1 = Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
        )
        val DEFAULT_WAYPOINT_2 = Location(
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
            DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
        )
    }

}
