package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.ui.formatter.TravelTimeFormatter
import hu.mostoha.mobile.android.huki.model.ui.OktDistanceFromMeUiModel
import hu.mostoha.mobile.android.huki.util.calculateDistance
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRouteGeometry
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.OktStampTag
import hu.mostoha.mobile.android.huki.model.domain.OktStampWaypoint
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesSavedStateUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktStampUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.util.DEFAULT_OKT_ROUTES
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.distanceBetween
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import org.junit.Test

class OktRoutesMapperTest {

    private val mapper = OktRoutesMapper()

    @Test
    fun `Given empty routes, when map, then empty UI model returns`() {
        val oktRoutes = OktRoutes(
            locations = emptyList(),
            stampWaypoints = emptyList(),
            oktRoutes = emptyList()
        )
        val oktType = OktType.OKT

        val oktRoutesUiModel = mapper.map(oktType, oktRoutes)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                oktType = oktType,
                mapGeoPoints = emptyList(),
                routes = emptyList(),
            )
        )

    }

    @Test
    fun `Given the first (full) OKT route, when map, then UI model returns`() {
        val oktFullGeoPoints = DEFAULT_OKT_POINTS
        val oktRoutes = OktRoutes(
            locations = oktFullGeoPoints,
            stampWaypoints = emptyList(),
            oktRoutes = listOf(DEFAULT_OKT_ROUTES.oktRoutes.first())
        )
        val oktRoute = DEFAULT_OKT_ROUTE
        val oktType = OktType.OKT

        val oktRoutesUiModel = mapper.map(oktType, oktRoutes)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                oktType = oktType,
                mapGeoPoints = oktFullGeoPoints.toGeoPoints(),
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT",
                        routeNumber = "",
                        routeName = "Írott-kő - Hollóháza",
                        geoPoints = DEFAULT_OKT_POINTS.toGeoPoints(),
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
                        stamps = emptyList(),
                        reversedStamps = emptyList(),
                        distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                        inclineText = DistanceFormatter.format(oktRoute.incline),
                        declineText = DistanceFormatter.format(oktRoute.decline),
                        travelTimeText = TravelTimeFormatter.formatHoursAndMinutes(oktRoute.travelTime).toMessage(),
                        detailsUrl = KEKTURA_OKT_URL,
                        isSelected = true,
                        isReversed = false,
                        isStarted = false,
                    )
                ),
            )
        )
    }

    @Test
    fun `Given the second (non-full) OKT route, when map, then UI model returns`() {
        val oktFullGeoPoints = DEFAULT_OKT_POINTS_2
        val oktRoutes = OktRoutes(
            locations = oktFullGeoPoints,
            stampWaypoints = emptyList(),
            oktRoutes = listOf(DEFAULT_OKT_ROUTES.oktRoutes[1])
        )
        val oktRoute = DEFAULT_OKT_ROUTE_2
        val oktType = OktType.OKT
        val oktRoutesUiModel = mapper.map(oktType, oktRoutes)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                oktType = oktType,
                mapGeoPoints = oktFullGeoPoints.toGeoPoints(),
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT-01",
                        routeNumber = "01",
                        routeName = "Írott-kő - Sárvár",
                        geoPoints = DEFAULT_OKT_POINTS_2.toGeoPoints(),
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
                        stamps = emptyList(),
                        reversedStamps = emptyList(),
                        distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                        inclineText = DistanceFormatter.format(oktRoute.incline),
                        declineText = DistanceFormatter.format(oktRoute.decline),
                        travelTimeText = TravelTimeFormatter.formatHoursAndMinutes(oktRoute.travelTime).toMessage(),
                        detailsUrl = KEKTURA_OKT_URL_TEMPLATE.format("okt-01"),
                        isSelected = false,
                        isReversed = false,
                        isStarted = false,
                    )
                ),
            )
        )
    }

    @Test
    fun `Given OKT section with stamp waypoints, when map, then stamps are mapped with on-track distance`() {
        val stampWaypoint = DEFAULT_STAMP_WAYPOINT.copy(location = DEFAULT_OKT_ROUTE_2.end)
        val oktRoutes = OktRoutes(
            locations = DEFAULT_OKT_POINTS_2,
            stampWaypoints = listOf(stampWaypoint),
            oktRoutes = listOf(OktRouteGeometry(DEFAULT_OKT_ROUTE_2, DEFAULT_OKT_POINTS_2, listOf(stampWaypoint)))
        )

        val oktRoutesUiModel = mapper.map(OktType.OKT, oktRoutes)

        assertThat(oktRoutesUiModel.routes.single().stamps).isEqualTo(
            listOf(
                OktStampUiModel(
                    stampTag = "OKTPH_02",
                    title = DEFAULT_STAMP_WAYPOINT.title,
                    description = DEFAULT_STAMP_WAYPOINT.description,
                    geoPoint = stampWaypoint.location.toGeoPoint(),
                    distanceText = DistanceFormatter.formatRelative(
                        DEFAULT_OKT_ROUTE_2.start.distanceBetween(DEFAULT_OKT_ROUTE_2.end)
                    ),
                )
            )
        )
    }

    @Test
    fun `Given OKT section with stamp waypoints, when map, then reversed stamps are measured from the end`() {
        val start = DEFAULT_OKT_ROUTE_2.start
        val end = DEFAULT_OKT_ROUTE_2.end
        val stampAtStart = DEFAULT_STAMP_WAYPOINT.copy(location = start)
        val stampAtEnd = DEFAULT_STAMP_WAYPOINT.copy(location = end)
        val oktRoutes = OktRoutes(
            locations = DEFAULT_OKT_POINTS_2,
            stampWaypoints = listOf(stampAtStart, stampAtEnd),
            oktRoutes = listOf(
                OktRouteGeometry(DEFAULT_OKT_ROUTE_2, DEFAULT_OKT_POINTS_2, listOf(stampAtStart, stampAtEnd))
            )
        )

        val route = mapper.map(OktType.OKT, oktRoutes).routes.single()

        assertThat(route.reversedStamps.map { it.geoPoint }).containsExactly(
            end.toGeoPoint(),
            start.toGeoPoint(),
        ).inOrder()
        assertThat(route.reversedStamps.map { it.distanceText }).containsExactly(
            DistanceFormatter.formatRelative(0),
            DistanceFormatter.formatRelative(start.distanceBetween(end)),
        ).inOrder()
    }

    @Test
    fun `Given my location on the track, when mapDistanceFromMe, then on-track distance and travel time return`() {
        val track = listOf(Location(47.0, 19.0, 100.0), Location(47.01, 19.0, 200.0), Location(47.02, 19.0, 150.0))
        val target = track.last().toGeoPoint()

        val distanceFromMe = mapper.mapDistanceFromMe(track, Location(47.0, 19.001), target)

        assertThat(distanceFromMe).isEqualTo(
            OktDistanceFromMeUiModel(
                geoPoint = target,
                distanceText = DistanceFormatter.format(track.calculateDistance()),
                travelTimeText = TravelTimeFormatter.format(track.calculateTravelTime()),
            )
        )
    }

    @Test
    fun `Given my location far from the track, when mapDistanceFromMe, then empty distance returns`() {
        val track = listOf(Location(47.0, 19.0), Location(47.01, 19.0), Location(47.02, 19.0))
        val target = track.last().toGeoPoint()

        val distanceFromMe = mapper.mapDistanceFromMe(track, Location(47.5, 19.0), target)

        assertThat(distanceFromMe).isEqualTo(
            OktDistanceFromMeUiModel(geoPoint = target, distanceText = null, travelTimeText = null)
        )
    }

    @Test
    fun `Given OKT routes UI model, when mapSavedState, then selected, started and reversed routes are saved`() {
        val routes = mapper.map(OktType.OKT, DEFAULT_OKT_ROUTES)
        val uiModel = routes.copy(
            routes = routes.routes.map { route ->
                val isOkt01 = route.oktId == "OKT-01"
                route.copy(isSelected = isOkt01, isStarted = isOkt01, isReversed = isOkt01)
            }
        )

        assertThat(mapper.mapSavedState(uiModel)).isEqualTo(
            OktRoutesSavedStateUiModel(
                oktType = OktType.OKT,
                selectedOktId = "OKT-01",
                isStarted = true,
                isReversed = true,
            )
        )
    }

    @Test
    fun `Given saved state, when applySavedState, then selected, started and reversed routes are restored`() {
        val routes = mapper.map(OktType.OKT, DEFAULT_OKT_ROUTES)
        val savedState = OktRoutesSavedStateUiModel(
            oktType = OktType.OKT,
            selectedOktId = "OKT-01",
            isStarted = true,
            isReversed = true,
        )

        val restored = mapper.applySavedState(routes, savedState).routes

        assertThat(restored[0].isSelected).isFalse()
        assertThat(restored[1].isSelected).isTrue()
        assertThat(restored[1].isStarted).isTrue()
        assertThat(restored[1].isReversed).isTrue()
    }

    @Test
    fun `Given saved state with unknown route, when applySavedState, then routes are unchanged`() {
        val routes = mapper.map(OktType.OKT, DEFAULT_OKT_ROUTES)
        val savedState = OktRoutesSavedStateUiModel(
            oktType = OktType.OKT,
            selectedOktId = "OKT-99",
            isStarted = true,
            isReversed = false,
        )

        assertThat(mapper.applySavedState(routes, savedState)).isEqualTo(routes)
    }

    @Test
    fun `Given the full OKT route with stamp waypoints, when map, then stamps are empty`() {
        val oktRoutes = OktRoutes(
            locations = DEFAULT_OKT_POINTS,
            stampWaypoints = listOf(DEFAULT_STAMP_WAYPOINT),
            oktRoutes = listOf(OktRouteGeometry(DEFAULT_OKT_ROUTE, DEFAULT_OKT_POINTS, listOf(DEFAULT_STAMP_WAYPOINT)))
        )

        val oktRoutesUiModel = mapper.map(OktType.OKT, oktRoutes)

        assertThat(oktRoutesUiModel.routes.single().stamps).isEmpty()
    }

    @Test
    fun `Given invalid OKT route, when map, then UI model returns with empty routes`() {
        val oktFullGeoPoints = DEFAULT_OKT_POINTS_2
        val oktRoutes = OktRoutes(
            locations = oktFullGeoPoints,
            stampWaypoints = emptyList(),
            oktRoutes = emptyList()
        )
        val oktType = OktType.OKT

        val oktRoutesUiModel = mapper.map(oktType, oktRoutes)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                oktType = oktType,
                mapGeoPoints = oktFullGeoPoints.toGeoPoints(),
                routes = emptyList(),
            )
        )
    }

    @Test
    fun `Given GPX waypoints, when map, then OKT stamp waypoints returns ordered by stamp number`() {
        val gpxWaypoint1 = WayPoint.Builder()
            .setName("Írott-kő 2")
            .setDesc("Írott-kői kilátó 2 - (OKTPH_01_2)")
            .setLatitude(47.352921667)
            .setLongitude(16.434327593)
            .build() as WayPoint
        val gpxWaypoint2 = WayPoint.Builder()
            .setName("Írott-kő")
            .setDesc("Írott-kői kilátó - (OKTPH_01)")
            .setLatitude(47.352921667)
            .setLongitude(16.434327593)
            .build() as WayPoint
        val oktType = OktType.OKT

        val stampWaypoints = mapper.map(oktType, listOf(gpxWaypoint1, gpxWaypoint2))

        assertThat(stampWaypoints).isEqualTo(
            listOf(
                OktStampWaypoint(
                    title = "Írott-kő",
                    description = "Írott-kői kilátó - (OKTPH_01)",
                    location = Location(47.352921667, 16.434327593),
                    stampTag = OktStampTag(
                        stampTag = "OKTPH_01",
                        stampNumber = 1.0,
                    ),
                ),
                OktStampWaypoint(
                    title = "Írott-kő 2",
                    description = "Írott-kői kilátó 2 - (OKTPH_01_2)",
                    location = Location(47.352921667, 16.434327593),
                    stampTag = OktStampTag(
                        stampTag = "OKTPH_01_2",
                        stampNumber = 1.2,
                    ),
                ),
            )
        )
    }

    @Test
    fun `Given GPX waypoints with suffixed and shared stamp tags, when map, then full stamp tags return`() {
        val gpxWaypoints = listOf(
            "Írott-kői kilátó - (OKTPH_01_DDKPH_01_2)",
            "Kőszeg - (OKTPH_03_B)",
            "Bakonybél - (OKTPH_101_B_1)",
        ).map { description ->
            WayPoint.Builder()
                .setName("Stamp")
                .setDesc(description)
                .setLatitude(47.35)
                .setLongitude(16.43)
                .build() as WayPoint
        }

        val stampWaypoints = mapper.map(OktType.OKT, gpxWaypoints)

        assertThat(stampWaypoints.map { it.stampTag }).containsExactly(
            OktStampTag(stampTag = "OKTPH_01_DDKPH_01_2", stampNumber = 1.0),
            OktStampTag(stampTag = "OKTPH_03_B", stampNumber = 3.0),
            OktStampTag(stampTag = "OKTPH_101_B_1", stampNumber = 101.0),
        ).inOrder()
    }

    @Test
    fun `Given GPX waypoints with same stamp number, when map, then stamps return ordered by stamp tag`() {
        val gpxWaypoints = listOf(
            "Írott-kői kilátó - (OKTPH_01_DDKPH_01_2)",
            "Írott-kői kilátó - (OKTPH_01_DDKPH_01_1)",
        ).map { description ->
            WayPoint.Builder()
                .setName("Stamp")
                .setDesc(description)
                .setLatitude(47.35)
                .setLongitude(16.43)
                .build() as WayPoint
        }

        val stampWaypoints = mapper.map(OktType.OKT, gpxWaypoints)

        assertThat(stampWaypoints.map { it.stampTag.stampTag }).containsExactly(
            "OKTPH_01_DDKPH_01_1",
            "OKTPH_01_DDKPH_01_2",
        ).inOrder()
    }

    @Test
    fun `Given RPDDK waypoint with shared OKT stamp tag, when map, then full stamp tag returns`() {
        val gpxWaypoint = WayPoint.Builder()
            .setName("Stamp")
            .setDesc("Írott-kői kilátó - (OKTPH_01_DDKPH_01_2)")
            .setLatitude(47.35)
            .setLongitude(16.43)
            .build() as WayPoint

        val stampWaypoints = mapper.map(OktType.RPDDK, listOf(gpxWaypoint))

        assertThat(stampWaypoints.single().stampTag).isEqualTo(
            OktStampTag(stampTag = "OKTPH_01_DDKPH_01_2", stampNumber = 1.2)
        )
    }

    companion object {
        private val DEFAULT_OKT_ROUTE = LOCAL_OKT_ROUTES.first()
        private val DEFAULT_OKT_POINTS = listOf(
            DEFAULT_OKT_ROUTE.start,
            DEFAULT_OKT_ROUTE.end,
        )
        private val DEFAULT_OKT_ROUTE_2 = LOCAL_OKT_ROUTES[1]
        private val DEFAULT_OKT_POINTS_2 = listOf(
            DEFAULT_OKT_ROUTE_2.start,
            DEFAULT_OKT_ROUTE_2.end,
        )
        private val DEFAULT_STAMP_WAYPOINT = OktStampWaypoint(
            title = "Hörmann-forrás",
            description = "Hörmann-forrás - (OKTPH_02)",
            location = Location(47.37, 16.47),
            stampTag = OktStampTag(stampTag = "OKTPH_02", stampNumber = 2.0),
        )
    }

}
