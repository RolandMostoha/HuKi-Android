package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
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
    address = PlaceAddress(
        name = "Dobogókő",
        street = "Fő utca",
        city = "Pilisszentkereszt",
        country = "Hungary",
        fullAddress = "Dobogókő, Fő utca, Pilisszentkereszt, Hungary"
    ),
    placeType = PlaceType.NODE,
    location = DEFAULT_PLACE_AREA_LOCATION
)

fun Location.toMockLocation(): AndroidLocation {
    val mockLocation = mockk<AndroidLocation>()

    every { mockLocation.latitude } returns this.latitude
    every { mockLocation.longitude } returns this.longitude

    return mockLocation
}

fun String.toTestPlaceArea(boundingBox: BoundingBox = DEFAULT_PLACE_AREA_BOX) = PlaceArea(
    placeAreaType = PlaceAreaType.MAP_SEARCH,
    location = DEFAULT_PLACE_AREA_LOCATION,
    boundingBox = boundingBox,
    addressMessage = this.toMessage(),
    distanceMessage = "2km".toMessage(),
    iconRes = 0
)