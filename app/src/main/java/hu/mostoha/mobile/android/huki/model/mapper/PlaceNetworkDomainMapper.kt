package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.photon.OsmType
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import javax.inject.Inject

class PlaceNetworkDomainMapper @Inject constructor() {

    companion object {
        private const val NORTH_EXTENT_POSITION = 1
        private const val EAST_EXTENT_POSITION = 2
        private const val SOUTH_EXTENT_POSITION = 3
        private const val WEST_EXTENT_POSITION = 0
    }

    fun mapPlace(response: PhotonQueryResponse, placeFeature: PlaceFeature): List<Place> {
        return response.features.mapNotNull { item ->
            val properties = item.properties

            val name = properties.name

            val city = properties.city ?: properties.county ?: properties.state ?: properties.country

            val street = listOfNotNull(
                properties.street,
                properties.houseNumber
            )
                .joinToString(" ")
                .ifEmpty { null }

            val address = listOfNotNull(
                properties.postCode,
                city,
                street,
            ).joinToString(" ")

            Place(
                osmId = properties.osmId.toString(),
                name = name ?: street ?: city ?: return@mapNotNull null,
                placeType = when (properties.osmType) {
                    OsmType.RELATION -> PlaceType.RELATION
                    OsmType.WAY -> PlaceType.WAY
                    OsmType.NODE -> PlaceType.NODE
                },
                location = Location(
                    latitude = item.geometry.coordinates[1],
                    longitude = item.geometry.coordinates[0]
                ),
                address = address,
                placeFeature = placeFeature,
                boundingBox = properties.extent?.let { extent ->
                    BoundingBox(
                        north = extent[NORTH_EXTENT_POSITION],
                        east = extent[EAST_EXTENT_POSITION],
                        south = extent[SOUTH_EXTENT_POSITION],
                        west = extent[WEST_EXTENT_POSITION]
                    )
                },
            )
        }
    }

}
