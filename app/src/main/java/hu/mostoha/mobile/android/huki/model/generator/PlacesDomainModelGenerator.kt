package hu.mostoha.mobile.android.huki.model.generator

import androidx.annotation.VisibleForTesting
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
        private const val NORTH_EXTENT_POSITION = 1
        private const val EAST_EXTENT_POSITION = 2
        private const val SOUTH_EXTENT_POSITION = 3
        private const val WEST_EXTENT_POSITION = 0
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

    fun generateGeometryByNode(response: OverpassQueryResponse, osmId: String): Geometry {
        val nodeElement = response.elements.firstOrNull { element ->
            element.type == ElementType.NODE && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id)

        return Geometry.Node(
            osmId = osmId,
            location = Location(nodeElement.lat!!, nodeElement.lon!!)
        )
    }

    fun generateGeometryByWay(response: OverpassQueryResponse, osmId: String): Geometry {
        val wayElement = response.elements.firstOrNull { element ->
            element.type == ElementType.WAY && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id)

        return generateWayGeometry(wayElement.id.toString(), wayElement.geometry ?: emptyList())
    }

    fun generateGeometryByRelation(response: OverpassQueryResponse, osmId: String): Geometry {
        val relationElement = response.elements.firstOrNull { element ->
            element.type == ElementType.RELATION && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id)

        val ways = relationElement.members?.mapNotNull { member ->
            val geometry = member.geometry
            if (geometry.isNullOrEmpty()) {
                null
            } else {
                generateWayGeometry(member.ref, geometry)
            }
        } ?: emptyList()

        return Geometry.Relation(osmId, ways)
    }

    private fun generateWayGeometry(wayId: String, geometry: List<Geom>): Geometry.Way {
        val locations = extractLocations(geometry)

        return Geometry.Way(
            osmId = wayId,
            locations = locations,
            distance = locations.calculateDistance()
        )
    }

    fun generateHikingRoutes(response: OverpassQueryResponse): List<HikingRoute> {
        return response.elements.mapNotNull { element ->
            val name = element.tags?.name
            val symbolType = element.tags?.jel

            if (name == null || symbolType == null) {
                return@mapNotNull null
            }

            HikingRoute(
                osmId = element.id.toString(),
                name = name,
                symbolType = symbolType
            )
        }
    }

    @VisibleForTesting
    internal fun extractLocations(geometries: List<Geom>): List<Location> {
        return geometries.mapNotNull {
            val latitude = it.lat
            val longitude = it.lon

            if (latitude != null && longitude != null) {
                Location(latitude, longitude)
            } else {
                null
            }
        }
    }

}
