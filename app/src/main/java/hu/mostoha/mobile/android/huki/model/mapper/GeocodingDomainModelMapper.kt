package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.photon.OsmType
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import javax.inject.Inject

class GeocodingDomainModelMapper @Inject constructor() {

    companion object {
        private const val NORTH_EXTENT_POSITION = 1
        private const val EAST_EXTENT_POSITION = 2
        private const val SOUTH_EXTENT_POSITION = 3
        private const val WEST_EXTENT_POSITION = 0
    }

    fun mapPlace(response: PhotonQueryResponse): List<Place> {
        return response.features.mapNotNull { item ->
            val name = item.properties.name

            val streetParts = listOfNotNull(
                item.properties.street,
                item.properties.houseNumber
            )
            val street = if (streetParts.isNotEmpty()) {
                streetParts.joinToString(" ")
            } else {
                null
            }

            Place(
                osmId = item.properties.osmId.toString(),
                name = name ?: street ?: item.properties.city ?: return@mapNotNull null,
                placeType = when (item.properties.osmType) {
                    OsmType.RELATION -> PlaceType.RELATION
                    OsmType.WAY -> PlaceType.WAY
                    OsmType.NODE -> PlaceType.NODE
                },
                location = Location(
                    latitude = item.geometry.coordinates[1],
                    longitude = item.geometry.coordinates[0]
                ),
                boundingBox = item.properties.extent?.let { extent ->
                    BoundingBox(
                        north = extent[NORTH_EXTENT_POSITION],
                        east = extent[EAST_EXTENT_POSITION],
                        south = extent[SOUTH_EXTENT_POSITION],
                        west = extent[WEST_EXTENT_POSITION]
                    )
                },
                country = item.properties.country,
                county = item.properties.county,
                district = item.properties.district,
                postCode = item.properties.postCode,
                city = item.properties.city,
                street = street
            )
        }
    }

}
