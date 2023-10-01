package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toLocationsTriple
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_OPEN
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ROUTE_PLAN_WAYPOINT_1_NAME
import hu.mostoha.mobile.android.huki.util.calculateDecline
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.util.calculateIncline
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import io.ticofab.androidgpxparser.parser.domain.Gpx
import io.ticofab.androidgpxparser.parser.domain.Route
import io.ticofab.androidgpxparser.parser.domain.RoutePoint
import io.ticofab.androidgpxparser.parser.domain.Track
import io.ticofab.androidgpxparser.parser.domain.TrackPoint
import io.ticofab.androidgpxparser.parser.domain.TrackSegment
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration

class LayersDomainModelMapperTest {

    private val mapper = LayersDomainModelMapper()

    @Test
    fun `Given GPX with closed track points, when mapGpxDetails, then GPX details returns`() {
        val fileName = "dera_szurdok.gpx"
        val gpx = DEFAULT_GPX_CLOSED

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        val expectedLocations = DEFAULT_GPX_WAY_CLOSED.toLocationsTriple()

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                gpxWaypoints = emptyList(),
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(
                    expectedLocations.mapNotNull { it.altitude }
                        .min()
                        .toInt(),
                    expectedLocations.mapNotNull { it.altitude }
                        .max()
                        .toInt()
                ),
                incline = expectedLocations.calculateIncline(),
                decline = expectedLocations.calculateDecline(),
            )
        )
    }

    @Test
    fun `Given GPX track points without altitude, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok_without_altitude.gpx"
        val gpx = DEFAULT_GPX_WITHOUT_ALTITUDE

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        val expectedLocations = DEFAULT_GPX_WAY_OPEN.map { Location(it.first, it.second) }

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                gpxWaypoints = emptyList(),
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(0, 0),
                incline = 0,
                decline = 0,
            )
        )
    }

    @Test
    fun `Given GPX with routes, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok_routes.gpx"
        val gpx = DEFAULT_GPX_ROUTES

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        val expectedLocations = DEFAULT_GPX_WAY_OPEN.toLocationsTriple()

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = expectedLocations,
                gpxWaypoints = mapper.mapGpxWaypoints(gpx.wayPoints),
                travelTime = expectedLocations.calculateTravelTime(),
                distance = expectedLocations.calculateDistance(),
                altitudeRange = Pair(
                    expectedLocations.mapNotNull { it.altitude }
                        .min()
                        .toInt(),
                    expectedLocations.mapNotNull { it.altitude }
                        .max()
                        .toInt()
                ),
                incline = expectedLocations.calculateIncline(),
                decline = expectedLocations.calculateDecline(),
            )
        )
    }

    @Test
    fun `Given GPX with waypoints only, when mapGpxDetails, then GPX Details returns`() {
        val fileName = "dera_szurdok_routes.gpx"
        val gpx = DEFAULT_GPX_WAYPOINTS_ONLY

        val gpxDetails = mapper.mapGpxDetails(fileName, gpx)

        assertThat(gpxDetails).isEqualTo(
            GpxDetails(
                id = gpxDetails.id,
                fileName = fileName,
                locations = emptyList(),
                gpxWaypoints = listOf(
                    GpxWaypoint(
                        location = DEFAULT_GPX_WAY_OPEN.toLocationsTriple().first(),
                        name = DEFAULT_ROUTE_PLAN_WAYPOINT_1_NAME,
                        description = "Description\nCmt",
                    )
                ),
                travelTime = Duration.ZERO,
                distance = 0,
                altitudeRange = Pair(0, 0),
                incline = 0,
                decline = 0,
            )
        )
    }

    @Test
    fun `Given GPX with empty track points, when mapGpxDetails, then GPX parse failed exception throws`() {
        val fileName = "dera_szurdok_empty.gpx"
        val gpx = Gpx.Builder()
            .setWayPoints(emptyList())
            .setRoutes(emptyList())
            .setTracks(emptyList())
            .build()

        val exception = assertThrows(GpxParseFailedException::class.java) {
            mapper.mapGpxDetails(fileName, gpx)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_gpx_parse_failed.toMessage())
    }

    companion object {
        private val DEFAULT_GPX_CLOSED = createGpxTrack(DEFAULT_GPX_WAY_CLOSED.toLocationsTriple())
        private val DEFAULT_GPX_WITHOUT_ALTITUDE = createGpxTrack(DEFAULT_GPX_WAY_OPEN.toLocationsTriple(), false)
        private val DEFAULT_GPX_ROUTES = createGpxRoute(DEFAULT_GPX_WAY_OPEN.toLocationsTriple())
        private val DEFAULT_GPX_WAYPOINTS_ONLY = createWaypoints(DEFAULT_GPX_WAY_OPEN.toLocationsTriple())

        private fun createGpxTrack(locations: List<Location>, withElevation: Boolean = true): Gpx {
            return Gpx.Builder()
                .setWayPoints(emptyList())
                .setRoutes(emptyList())
                .setTracks(
                    listOf(
                        Track.Builder()
                            .setTrackSegments(
                                listOf(
                                    TrackSegment.Builder()
                                        .setTrackPoints(
                                            locations.map { location ->
                                                TrackPoint.Builder()
                                                    .setLatitude(location.latitude)
                                                    .setLongitude(location.longitude)
                                                    .setElevation(
                                                        if (withElevation) {
                                                            location.altitude
                                                        } else {
                                                            null
                                                        }
                                                    )
                                                    .build() as TrackPoint
                                            }
                                        )
                                        .build()
                                )
                            )
                            .build()
                    )
                )
                .build()
        }

        private fun createGpxRoute(locations: List<Location>): Gpx {
            return Gpx.Builder()
                .setWayPoints(
                    listOf(
                        WayPoint.Builder()
                            .setName(DEFAULT_ROUTE_PLAN_WAYPOINT_1_NAME)
                            .setLatitude(locations.first().latitude)
                            .setLongitude(locations.first().longitude)
                            .setElevation(locations.first().altitude)
                            .build() as WayPoint,
                    )
                )
                .setRoutes(
                    listOf(
                        Route.Builder()
                            .setRoutePoints(
                                locations.map {
                                    RoutePoint.Builder()
                                        .setLatitude(it.latitude)
                                        .setLongitude(it.longitude)
                                        .setElevation(it.altitude)
                                        .build() as RoutePoint
                                }
                            )
                            .build()
                    )
                )
                .setTracks(emptyList())
                .build()
        }

        private fun createWaypoints(locations: List<Location>): Gpx {
            return Gpx.Builder()
                .setWayPoints(
                    listOf(
                        WayPoint.Builder()
                            .setName(DEFAULT_ROUTE_PLAN_WAYPOINT_1_NAME)
                            .setDesc("Description")
                            .setCmt("Cmt")
                            .setLatitude(locations.first().latitude)
                            .setLongitude(locations.first().longitude)
                            .setElevation(locations.first().altitude)
                            .build() as WayPoint,
                    )
                )
                .setRoutes(emptyList())
                .setTracks(emptyList())
                .build()
        }
    }

}
