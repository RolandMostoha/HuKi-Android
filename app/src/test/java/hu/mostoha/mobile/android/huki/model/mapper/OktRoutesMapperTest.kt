package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.constants.KEKTURA_ROUTE_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.constants.KEKTURA_URL
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.OktRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import org.junit.Test
import org.osmdroid.util.GeoPoint
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class OktRoutesMapperTest {

    private val mapper = OktRoutesMapper()

    @Test
    fun `Given empty routes, when map, then empty UI model returns`() {
        val oktFullGeoPoints = emptyList<GeoPoint>()
        val oktRoutes = emptyList<OktRoute>()

        val oktRoutesUiModel = mapper.map(oktFullGeoPoints, oktRoutes)

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints,
                routes = emptyList(),
            )
        )

    }

    @Test
    fun `Given the first (full) OKT route, when map, then UI model returns`() {
        val oktFullGeoPoints = DEFAULT_GEO_POINTS
        val oktRoute = DEFAULT_OKT_ROUTE

        val oktRoutesUiModel = mapper.map(oktFullGeoPoints, listOf(oktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints,
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT",
                        routeNumber = "",
                        routeName = "Írott-kő - Hollóháza",
                        geoPoints = DEFAULT_GEO_POINTS,
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
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
        val oktFullGeoPoints = DEFAULT_GEO_POINTS_2
        val oktRoute = DEFAULT_OKT_ROUTE_2

        val oktRoutesUiModel = mapper.map(oktFullGeoPoints, listOf(oktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints,
                routes = listOf(
                    OktRouteUiModel(
                        oktId = "OKT-01",
                        routeNumber = "01",
                        routeName = "Írott-kő - Sárvár",
                        geoPoints = DEFAULT_GEO_POINTS_2,
                        start = oktRoute.start.toGeoPoint(),
                        end = oktRoute.end.toGeoPoint(),
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
        val oktFullGeoPoints = DEFAULT_GEO_POINTS_2
        val invalidOktRoute = OktRoute(
            id = "okt-fake",
            name = "OKT fake",
            distanceKm = 1172.5,
            incline = 31455,
            decline = 32035,
            travelTime = 344.hours.plus(40.minutes),
            start = Location(latitude = 47.0, longitude = 16.0),
            end = Location(latitude = 48.0, longitude = 21.0)
        )

        val oktRoutesUiModel = mapper.map(oktFullGeoPoints, listOf(invalidOktRoute))

        assertThat(oktRoutesUiModel).isEqualTo(
            OktRoutesUiModel(
                mapGeoPoints = oktFullGeoPoints,
                routes = emptyList(),
            )
        )
    }

    companion object {
        private val DEFAULT_OKT_ROUTE = LOCAL_OKT_ROUTES.first()
        private val DEFAULT_GEO_POINTS = listOf(
            DEFAULT_OKT_ROUTE.start.toGeoPoint(),
            DEFAULT_OKT_ROUTE.end.toGeoPoint(),
        )
        private val DEFAULT_OKT_ROUTE_2 = LOCAL_OKT_ROUTES[1]
        private val DEFAULT_GEO_POINTS_2 = listOf(
            DEFAULT_OKT_ROUTE_2.start.toGeoPoint(),
            DEFAULT_OKT_ROUTE_2.end.toGeoPoint(),
        )
    }

}
