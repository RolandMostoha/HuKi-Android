package hu.mostoha.mobile.android.huki.model.mapper

import androidx.annotation.VisibleForTesting
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.network.overpass.ElementType
import hu.mostoha.mobile.android.huki.model.network.overpass.Geom
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.util.calculateDistance
import javax.inject.Inject

class PlaceDetailsNetworkDomainMapper @Inject constructor() {

    fun mapGeometryByNode(response: OverpassQueryResponse, osmId: String): Geometry {
        val nodeElement = response.elements.firstOrNull { element ->
            element.type == ElementType.NODE && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id.toMessage())

        return Geometry.Node(
            osmId = osmId,
            location = Location(nodeElement.lat!!, nodeElement.lon!!)
        )
    }

    fun mapGeometryByWay(response: OverpassQueryResponse, osmId: String): Geometry {
        val wayElement = response.elements.firstOrNull { element ->
            element.type == ElementType.WAY && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id.toMessage())

        return mapWayGeometry(wayElement.id.toString(), wayElement.geometry ?: emptyList())
    }

    fun mapGeometryByRelation(response: OverpassQueryResponse, osmId: String): Geometry {
        val relationElement = response.elements.firstOrNull { element ->
            element.type == ElementType.RELATION && element.id.toString() == osmId
        } ?: throw DomainException(R.string.error_message_missing_osm_id.toMessage())

        val ways = relationElement.members?.mapNotNull { member ->
            val geometry = member.geometry
            if (geometry.isNullOrEmpty()) {
                null
            } else {
                mapWayGeometry(member.ref, geometry)
            }
        } ?: emptyList()

        return Geometry.Relation(osmId, ways)
    }

    private fun mapWayGeometry(wayId: String, geometry: List<Geom>): Geometry.Way {
        val locations = extractLocations(geometry)

        return Geometry.Way(
            osmId = wayId,
            locations = locations,
            distance = locations.calculateDistance()
        )
    }

    fun mapHikingRoutes(response: OverpassQueryResponse): List<HikingRoute> {
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
