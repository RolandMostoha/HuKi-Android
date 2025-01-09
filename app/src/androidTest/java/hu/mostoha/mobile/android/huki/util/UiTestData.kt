package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_LOCATION_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_PLACE_AREA_LOCATION_LONGITUDE
import io.mockk.every
import io.mockk.mockk
import kotlin.random.Random
import android.location.Location as AndroidLocation

fun Location.toMockLocation(): AndroidLocation {
    val mockLocation = mockk<AndroidLocation>(relaxed = true)

    every { mockLocation.latitude } returns this.latitude
    every { mockLocation.longitude } returns this.longitude
    every { mockLocation.altitude } returns (this.altitude ?: 0.0)
    every { mockLocation.accuracy } returns (Random.nextFloat())

    return mockLocation
}

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
