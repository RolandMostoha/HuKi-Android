package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceAddress
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.photon.OsmType
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import javax.inject.Inject

class PhotonPlaceNetworkMapper @Inject constructor() {

    companion object {
        private const val NORTH_EXTENT_POSITION = 1
        private const val EAST_EXTENT_POSITION = 2
        private const val SOUTH_EXTENT_POSITION = 3
        private const val WEST_EXTENT_POSITION = 0
    }

    fun mapPlaceProfile(response: PhotonQueryResponse): List<PlaceProfile> {
        return response.features
            .mapNotNull { item ->
                val properties = item.properties
                val city = properties.city ?: properties.county
                val country = properties.state ?: properties.country
                val street = listOfNotNull(
                    properties.street,
                    properties.houseNumber
                )
                    .joinToString(" ")
                    .ifEmpty { null }
                val fullAddress = listOfNotNull(
                    properties.postCode,
                    city,
                    street,
                ).joinToString(" ")
                val name = properties.name ?: street ?: city ?: return@mapNotNull null

                PlaceProfile(
                    osmId = properties.osmId.toString(),
                    placeType = when (properties.osmType) {
                        OsmType.RELATION -> PlaceType.RELATION
                        OsmType.WAY -> PlaceType.WAY
                        OsmType.NODE -> PlaceType.NODE
                    },
                    displayName = name,
                    displayAddress = fullAddress,
                    location = Location(
                        latitude = item.geometry.coordinates[1],
                        longitude = item.geometry.coordinates[0]
                    ),
                    address = PlaceAddress(
                        houseNumber = properties.houseNumber,
                        street = street,
                        city = city,
                        country = country,
                    ),
                    boundingBox = properties.extent?.let { extent ->
                        BoundingBox(
                            north = extent[NORTH_EXTENT_POSITION],
                            east = extent[EAST_EXTENT_POSITION],
                            south = extent[SOUTH_EXTENT_POSITION],
                            west = extent[WEST_EXTENT_POSITION]
                        )
                    }
                )
            }
    }

}
