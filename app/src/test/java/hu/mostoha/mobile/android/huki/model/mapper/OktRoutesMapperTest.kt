package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.constants.KEKTURA_ROUTE_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.constants.KEKTURA_URL
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
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
import io.ticofab.androidgpxparser.parser.domain.WayPoint
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OktRoutesMapperTest {

    private val mapper = OktRoutesMapper()

    @Test
    fun `Given empty routes, when map, then empty UI model returns`() {
        val oktRoutes = OktRoutes(
            locations = emptyList(),
            stampWaypoints = emptyList(),
        )
        val oktRouteList = emptyList<OktRoute>()

        val oktRoutesUiModel = mapper.map(oktRoutes, oktRouteList)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
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
        )
        val oktRoute = DEFAULT_OKT_ROUTE

        val oktRoutesUiModel = mapper.map(oktRoutes, listOf(oktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints.toGeoPoints(),
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT",
                        routeNumber = "",
                        routeName = "Írott-kő - Hollóháza",
                        geoPoints = DEFAULT_OKT_POINTS.toGeoPoints(),
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
                        stampWaypoints = emptyList(),
                        distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                        inclineText = DistanceFormatter.formatSigned(oktRoute.incline),
                        declineText = DistanceFormatter.formatSigned(-1 * oktRoute.decline),
                        travelTimeText = oktRoute.travelTime.formatHoursAndMinutes().toMessage(),
                        detailsUrl = KEKTURA_URL,
                        isSelected = true,
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
        )
        val oktRoute = DEFAULT_OKT_ROUTE_2

        val oktRoutesUiModel = mapper.map(oktRoutes, listOf(oktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints.toGeoPoints(),
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT-01",
                        routeNumber = "01",
                        routeName = "Írott-kő - Sárvár",
                        geoPoints = DEFAULT_OKT_POINTS_2.toGeoPoints(),
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
                        stampWaypoints = emptyList(),
                        distanceText = DistanceFormatter.formatKm(oktRoute.distanceKm.toInt()),
                        inclineText = DistanceFormatter.formatSigned(oktRoute.incline),
                        declineText = DistanceFormatter.formatSigned(-1 * oktRoute.decline),
                        travelTimeText = oktRoute.travelTime.formatHoursAndMinutes().toMessage(),
                        detailsUrl = KEKTURA_ROUTE_URL_TEMPLATE.format("okt-01"),
                        isSelected = false,
                    )
                ),
            )
        )
    }

    @Test
    fun `Given invalid OKT route, when map, then UI model returns with empty routes`() {
        val oktFullGeoPoints = DEFAULT_OKT_POINTS_2
        val oktRoutes = OktRoutes(
            locations = oktFullGeoPoints,
            stampWaypoints = emptyList(),
        )
        val invalidOktRoute = OktRoute(
            id = "okt-fake",
            name = "OKT fake",
            distanceKm = 1172.5,
            incline = 31455,
            decline = 32035,
            travelTime = 344.hours.plus(40.minutes),
            start = Location(latitude = 47.0, longitude = 16.0),
            end = Location(latitude = 48.0, longitude = 21.0),
            stampTagsRange = 1.0..2.0
        )

        val oktRoutesUiModel = mapper.map(oktRoutes, listOf(invalidOktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
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

        val stampWaypoints = mapper.map(listOf(gpxWaypoint1, gpxWaypoint2))

        assertThat(stampWaypoints).isEqualTo(
            listOf(
                OktStampWaypoint(
                    title = "Írott-kő",
                    description = "Írott-kői kilátó - (OKTPH_01)",
                    location = Location(47.352921667, 16.434327593),
                    stampTag = "(OKTPH_01)",
                    stampNumber = 1.0,
                ),
                OktStampWaypoint(
                    title = "Írott-kő 2",
                    description = "Írott-kői kilátó 2 - (OKTPH_01_2)",
                    location = Location(47.352921667, 16.434327593),
                    stampTag = "(OKTPH_01_2)",
                    stampNumber = 1.2,
                ),
            )
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
    }

}
