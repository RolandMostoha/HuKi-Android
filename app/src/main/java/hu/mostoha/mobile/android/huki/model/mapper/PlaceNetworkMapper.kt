package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.locationiq.LocationIqPlace
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_EAST_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_NORTH_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_SOUTH_INDEX
import hu.mostoha.mobile.android.huki.network.LocationIqService.Companion.LOCATION_IQ_BB_WEST_INDEX
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import timber.log.Timber
import javax.inject.Inject

class PlaceNetworkMapper @Inject constructor() {

    fun mapPlaceProfile(response: LocationIqPlace): PlaceProfile {
        val location = Location(response.lat, response.lon)
        val address = listOfNotNull(
            response.address?.postcode,
            response.address?.city ?: response.address?.county ?: response.address?.country,
            listOfNotNull(
                response.address?.road,
                response.address?.houseNumber
            )
                .joinToString(" ")
                .ifEmpty { null }
        )
            .joinToString(" ")
            .ifEmpty { null }
        val displayNameFallback = response.displayName
            .split(",")
            .take(2)
            .joinToString(" ")
        val displayName = response.displayPlace ?: response.address?.name ?: displayNameFallback
        val displayAddress = address ?: response.displayAddress ?: LocationFormatter.formatString(location)

        return PlaceProfile(
            osmId = response.osmId,
            placeType = mapPlaceType(response),
            location = location,
            displayName = displayName,
            displayAddress = displayAddress,
            address = PlaceAddress(
                houseNumber = response.address?.houseNumber,
                street = response.address?.road,
                city = response.address?.city,
                country = response.address?.country,
            ),
            boundingBox = response.boundingBox?.let { doubles ->
                BoundingBox(
                    north = doubles[LOCATION_IQ_BB_NORTH_INDEX],
                    east = doubles[LOCATION_IQ_BB_EAST_INDEX],
                    south = doubles[LOCATION_IQ_BB_SOUTH_INDEX],
                    west = doubles[LOCATION_IQ_BB_WEST_INDEX]
                )
            }
        )
    }

    fun mapPlaces(response: List<LocationIqPlace>): List<PlaceProfile> {
        return response.map { mapPlaceProfile(it) }
    }

    private fun mapPlaceType(response: LocationIqPlace): PlaceType {
        return when (response.osmType) {
            "node" -> PlaceType.NODE
            "way" -> PlaceType.WAY
            "relation" -> PlaceType.RELATION
            else -> {
                Timber.w("Unknown LocationIQ OSM type: ${response.osmType}")
                PlaceType.NODE
            }
        }
    }

}
