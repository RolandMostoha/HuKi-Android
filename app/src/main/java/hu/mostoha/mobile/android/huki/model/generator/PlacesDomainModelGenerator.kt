package hu.mostoha.mobile.android.huki.model.generator

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.DomainException
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.network.*
import hu.mostoha.mobile.android.huki.model.network.overpass.ElementType
import hu.mostoha.mobile.android.huki.model.network.overpass.Geom
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.model.network.photon.OsmType
import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import hu.mostoha.mobile.android.huki.util.calculateDistance
import javax.inject.Inject

class PlacesDomainModelGenerator @Inject constructor() {

    companion object {
        private const val NORTH_EXTENT_POSITION = 2
        private const val EAST_EXTENT_POSITION = 1
        private const val SOUTH_EXTENT_POSITION = 0
        private const val WEST_EXTENT_POSITION = 3
    }

    fun generatePlace(response: PhotonQueryResponse): List<Place> {
        return response.features.map {
            Place(
                osmId = it.properties.osmId.toString(),
                name = it.properties.name,
                placeType = when (it.properties.osmType) {
                    OsmType.RELATION -> PlaceType.RELATION
                    OsmType.WAY -> PlaceType.WAY
                    OsmType.NODE -> PlaceType.NODE
                },
                location = Location(
                    latitude = it.geometry.coordinates[1],
                    longitude = it.geometry.coordinates[0]
                ),
                boundingBox = it.properties.extent?.let { extent ->
                    BoundingBox(
                        north = extent[NORTH_EXTENT_POSITION],
                        east = extent[EAST_EXTENT_POSITION],
                        south = extent[SOUTH_EXTENT_POSITION],
                        west = extent[WEST_EXTENT_POSITION]
                    )
                },
                country = it.properties.country,
                county = it.properties.county,
                district = it.properties.district,
                postCode = it.properties.postCode,
                city = it.properties.city,
                street = listOfNotNull(it.properties.street, it.properties.houseNumber).joinToString(" ")
            )
        }
    }

    fun generatePlaceDetailsByNode(response: OverpassQueryResponse): PlaceDetails {
        val nodeElement = response.elements.first()

        return PlaceDetails(
            osmId = nodeElement.id.toString(),
            payload = Payload.Node(Location(nodeElement.lat!!, nodeElement.lon!!))
        )
    }

    fun generatePlaceDetailsByWay(response: OverpassQueryResponse, osmId: String): PlaceDetails {
        val wayElement = response.elements.firstOrNull {
            it.type == ElementType.WAY && it.id.toString() == osmId
        } ?: TODO()
        val wayId = wayElement.id.toString()
        val locations = wayElement.geometry?.extractLocations() ?: emptyList()

        return PlaceDetails(
            osmId = wayId,
            payload = Payload.Way(
                osmId = wayId,
                locations = locations,
                distance = locations.calculateDistance()
            )
        )
    }

    fun generatePlaceDetailsByRelation(response: OverpassQueryResponse, osmId: String): PlaceDetails {
        val relationElement = response.elements.firstOrNull {
            it.type == ElementType.RELATION && it.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id)

        val ways = relationElement.members?.mapNotNull {
            val ref = it.ref
            val geometry = it.geometry
            if (ref != null && geometry != null) {
                val locations = geometry.extractLocations()
                Payload.Way(
                    osmId = ref,
                    locations = locations,
                    distance = locations.calculateDistance()
                )
            } else {
                null
            }
        } ?: emptyList()

        return PlaceDetails(
            osmId = relationElement.id.toString(),
            payload = Payload.Relation(ways)
        )
    }

    fun generateHikingRoutes(response: OverpassQueryResponse): List<HikingRoute> {
        return response.elements.mapNotNull {
            HikingRoute(
                osmId = it.id.toString(),
                name = it.tags?.name ?: return@mapNotNull null,
                symbolType = it.tags?.jel ?: return@mapNotNull null
            )
        }
    }

    private fun List<Geom>.extractLocations(): List<Location> {
        return mapNotNull {
            val lat = it.lat
            val lon = it.lon

            if (lat != null && lon != null) {
                Location(lat, lon)
            } else {
                null
            }
        }
    }

}
