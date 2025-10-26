package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.DESTINATIONS_BUKK
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRouteGeometry
import hu.mostoha.mobile.android.huki.model.domain.OktRoutes
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_LOCATION_LONGITUDE
import io.mockk.every
import io.mockk.mockk
import android.location.Location as AndroidLocation

val DEFAULT_PLACE_AREA_BOX = BoundingBox(
    north = DEFAULT_PLACE_AREA_BOX_NORTH,
    east = DEFAULT_PLACE_AREA_BOX_EAST,
    south = DEFAULT_PLACE_AREA_BOX_SOUTH,
    west = DEFAULT_PLACE_AREA_BOX_WEST
)

val DEFAULT_PLACE_AREA_LOCATION = Location(
    latitude = DEFAULT_PLACE_AREA_LOCATION_LATITUDE,
    longitude = DEFAULT_PLACE_AREA_LOCATION_LONGITUDE
)

val DEFAULT_PLACE_PROFILE = PlaceProfile(
    osmId = "369569761",
    displayName = "Dobogókő",
    displayAddress = "Dobogókő, Fő utca, Pilisszentkereszt, Hungary",
    address = PlaceAddress(
        street = "Fő utca",
        city = "Pilisszentkereszt",
        country = "Hungary",
    ),
    placeType = PlaceType.NODE,
    location = DEFAULT_PLACE_AREA_LOCATION
)

val DEFAULT_LANDSCAPE_UI_MODEL = LandscapeUiModel(
    osmId = LOCAL_LANDSCAPES.first().osmId,
    name = LOCAL_LANDSCAPES.first().nameRes.toMessage(),
    placeType = PlaceType.WAY,
    color = LOCAL_LANDSCAPES.first().color,
    geoPoint = LOCAL_LANDSCAPES.first().center.toGeoPoint(),
    iconRes = R.drawable.ic_landscapes_mountain_high,
    destinations = DESTINATIONS_BUKK,
)

private val DEFAULT_OKT_FULL_GEO_POINTS = listOf(
    LOCAL_OKT_ROUTES[0].start,
    LOCAL_OKT_ROUTES[0].end,
    LOCAL_OKT_ROUTES[1].start,
    LOCAL_OKT_ROUTES[1].end,
)

val DEFAULT_OKT_ROUTES = OktRoutes(
    locations = DEFAULT_OKT_FULL_GEO_POINTS,
    stampWaypoints = emptyList(),
    oktRoutes = listOf(
        OktRouteGeometry(
            LOCAL_OKT_ROUTES[0],
            DEFAULT_OKT_FULL_GEO_POINTS.subList(0, 2),
            emptyList()
        ),
        OktRouteGeometry(
            LOCAL_OKT_ROUTES[1],
            DEFAULT_OKT_FULL_GEO_POINTS.subList(2, 4),
            emptyList()
        )
    )
)

fun Location.toMockLocation(): AndroidLocation {
    val mockLocation = mockk<AndroidLocation>()

    every { mockLocation.latitude } returns this.latitude
    every { mockLocation.longitude } returns this.longitude
    every { mockLocation.altitude } returns (this.altitude ?: 0.0)

    return mockLocation
}

fun String.toTestPlaceArea(boundingBox: BoundingBox = DEFAULT_PLACE_AREA_BOX) = PlaceArea(
    placeAreaType = PlaceAreaType.MapSearch,
    location = DEFAULT_PLACE_AREA_LOCATION,
    boundingBox = boundingBox,
    addressMessage = this.toMessage(),
    distanceMessage = "2km".toMessage(),
    iconRes = 0
)
