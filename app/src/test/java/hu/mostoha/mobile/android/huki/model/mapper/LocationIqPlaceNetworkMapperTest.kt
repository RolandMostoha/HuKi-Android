package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.locationiq.LocationIqAddress
import hu.mostoha.mobile.android.huki.model.network.locationiq.LocationIqPlace
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_EAST_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_NORTH_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_SOUTH_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_WEST_INDEX
import org.junit.Test

class LocationIqPlaceNetworkMapperTest {

    private val mapper = LocationIqPlaceNetworkMapper()

    @Test
    fun `Given LocationIQ response, when mapPlaceProfile, then correct PlaceProfile returns`() {
        val locationIqPlace = DEFAULT_LOCATION_IQ_PLACE

        val placeProfile = mapper.mapPlaceProfile(locationIqPlace)

        assertThat(placeProfile).isEqualTo(
            PlaceProfile(
                osmId = locationIqPlace.osmId,
                placeType = PlaceType.WAY,
                location = Location(locationIqPlace.lat, locationIqPlace.lon),
                displayName = "Téry Ödön út  Pilisszentkereszt",
                displayAddress = "2099 Pilisszentkereszt Téry Ödön út",
                address = PlaceAddress(
                    houseNumber = locationIqPlace.address?.houseNumber,
                    street = locationIqPlace.address?.road,
                    city = locationIqPlace.address?.city,
                    country = locationIqPlace.address?.country,
                ),
                boundingBox = locationIqPlace.boundingBox?.let {
                    BoundingBox(
                        north = it[LOCATION_IQ_BB_NORTH_INDEX],
                        east = it[LOCATION_IQ_BB_EAST_INDEX],
                        south = it[LOCATION_IQ_BB_SOUTH_INDEX],
                        west = it[LOCATION_IQ_BB_WEST_INDEX]
                    )
                }
            )
        )
    }

    companion object {
        private val DEFAULT_LOCATION_IQ_PLACE = LocationIqPlace(
            placeId = "59760319",
            osmId = "1017319194",
            osmType = "way",
            licence = "https://locationiq.com/attribution",
            lat = 47.71769356609254,
            lon = 18.885611757341096,
            boundingBox = listOf(47.7176183, 47.7178382, 18.8852508, 18.8863375),
            displayName = "Téry Ödön út, Pilisszentkereszt, Szentendrei járás, Pest vármegye, Közép-Magyarország, 2099, Magyarország",
            displayAddress = "1508, Magyarország",
            address = LocationIqAddress(
                road = "Téry Ödön út",
                city = "Pilisszentkereszt",
                county = "Pest vármegye",
                state = "Közép-Magyarország",
                postcode = "2099",
                country = "Magyarország",
                countryCode = "hu"
            )
        )
    }

}
