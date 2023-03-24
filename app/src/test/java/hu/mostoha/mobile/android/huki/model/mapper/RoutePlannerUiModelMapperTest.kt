package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.RoutePlan
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.AltitudeUiModel
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointItem
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import org.junit.Test
import org.osmdroid.util.BoundingBox
import kotlin.time.Duration.Companion.minutes

class RoutePlannerUiModelMapperTest {

    private val mapper = RoutePlannerUiModelMapper()

    @Test
    fun `Given route plan domain model, when mapRoutePlan, then error item returns`() {
        val routePlanName = "HuKi_Dobogo_Ram_456"
        val routePlan = DEFAULT_ROUTE_PLAN
        val triggerLocations = DEFAULT_WAYPOINTS

        val model = mapper.mapToRoutePlanUiModel(routePlanName, triggerLocations, routePlan)

        assertThat(model).isEqualTo(
            RoutePlanUiModel(
                id = routePlan.id,
                name = routePlanName,
                triggerLocations = triggerLocations,
                wayPoints = listOf(
                    WaypointItem(
                        id = model.wayPoints[0].id,
                        order = 0,
                        waypointType = WaypointType.START,
                        location = DEFAULT_WAYPOINTS.first(),
                    ),
                    WaypointItem(
                        id = model.wayPoints[1].id,
                        order = 1,
                        waypointType = WaypointType.END,
                        location = DEFAULT_WAYPOINTS.last(),
                    )
                ),
                geoPoints = listOf(
                    DEFAULT_WAYPOINTS.first().toGeoPoint(),
                    DEFAULT_WAYPOINTS.last().toGeoPoint()
                ),
                boundingBox = BoundingBox.fromGeoPoints(
                    listOf(
                        DEFAULT_WAYPOINTS.first().toGeoPoint(),
                        DEFAULT_WAYPOINTS.last().toGeoPoint()
                    )
                ).toDomainBoundingBox(),
                travelTimeText = routePlan.travelTime.formatHoursAndMinutes().toMessage(),
                distanceText = DistanceFormatter.format(routePlan.distance),
                altitudeUiModel = AltitudeUiModel(
                    minAltitudeText = DistanceFormatter.format(routePlan.altitudeRange.first),
                    maxAltitudeText = DistanceFormatter.format(routePlan.altitudeRange.second),
                    uphillText = DistanceFormatter.format(routePlan.incline),
                    downhillText = DistanceFormatter.format(routePlan.decline),
                ),
                isClosed = routePlan.isClosed
            )
        )
    }

    @Test
    fun `Given waypoint items with string primary text, when mapToRoutePlanName, then formatted name returns`() {
        val startWaypointName = "Dobogókő"
        val endWaypointName = "Rám-hegy"
        val waypoints = listOf(
            WaypointItem(
                order = 0,
                waypointType = WaypointType.START,
                primaryText = startWaypointName.toMessage(),
                location = Location(
                    DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                    DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
                )
            ),
            WaypointItem(
                order = 1,
                waypointType = WaypointType.INTERMEDIATE,
                primaryText = endWaypointName.toMessage(),
                location = Location(
                    DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                    DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                )
            ),
            WaypointItem(
                order = 2,
                waypointType = WaypointType.END
            ),
        )

        val result = mapper.mapToRoutePlanName(waypoints)

        assertThat(result).contains("Dobogoko_Ram_hegy_HuKi")
    }

    @Test
    fun `Given waypoint items without string primary text, when mapToRoutePlanName, then formatted name returns with locations`() {
        val startWaypointName = R.string.place_finder_my_location_button.toMessage()
        val endWaypointName = "Barackos %^&".toMessage()
        val waypoints = listOf(
            WaypointItem(
                order = 0,
                waypointType = WaypointType.START,
                primaryText = startWaypointName,
                location = Location(
                    DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                    DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE
                )
            ),
            WaypointItem(
                order = 0,
                waypointType = WaypointType.END,
                primaryText = endWaypointName,
                location = Location(
                    DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                    DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                )
            ),
        )

        val result = mapper.mapToRoutePlanName(waypoints)

        assertThat(result).contains("47_72_18_89_Barackos___HuKi")
    }

    companion object {
        private val DEFAULT_WAYPOINTS = listOf(
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_1_ALTITUDE
            ),
            Location(
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LATITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_LONGITUDE,
                DEFAULT_ROUTE_PLAN_WAYPOINT_2_ALTITUDE
            )
        )
        private val DEFAULT_ROUTE_PLAN = RoutePlan(
            wayPoints = DEFAULT_WAYPOINTS,
            locations = DEFAULT_WAYPOINTS,
            travelTime = 100L.minutes,
            distance = 13000,
            altitudeRange = Pair(
                DEFAULT_WAYPOINTS.minOf { it.altitude!! }.toInt(),
                DEFAULT_WAYPOINTS.maxOf { it.altitude!! }.toInt()
            ),
            incline = 500,
            decline = 200,
            isClosed = false
        )
    }

}
