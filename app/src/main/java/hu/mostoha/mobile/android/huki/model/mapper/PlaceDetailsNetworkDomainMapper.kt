package hu.mostoha.mobile.android.huki.model.mapper

import androidx.annotation.VisibleForTesting
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OSM_TAG_NAME
import hu.mostoha.mobile.android.huki.model.domain.OsmTags
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.network.overpass.ElementType
import hu.mostoha.mobile.android.huki.model.network.overpass.Geom
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.network.overpass.center
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.util.EnumUtil.valueOf
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
            val tags = element.tags
            val name = tags?.get(OSM_TAG_NAME)
            val symbolType = SymbolType.entries.valueOf(tags?.get(OsmTags.JEL.osmKey)) ?: SymbolType.UNHANDLED

            if (name == null) {
                return@mapNotNull null
            }

            HikingRoute(
                osmId = element.id.toString(),
                name = name,
                symbolType = symbolType
            )
        }
    }

    fun mapPlacesByCategories(response: OverpassQueryResponse, categories: Set<PlaceCategory>): List<Place> {
        return response.elements
            .mapNotNull { element ->
                val tags = element.tags

                if (tags.isNullOrEmpty() || categories.isEmpty()) {
                    return@mapNotNull null
                }

                val placeCategory = if (categories.size == 1) {
                    categories.first()
                } else {
                    categories.firstOrNull { placeCategory ->
                        placeCategory.osmQueryTags.any { queryTag ->
                            val queryTagKey = queryTag.first
                            val queryTagValue = queryTag.second

                            if (queryTagValue != null) {
                                tags.containsKey(queryTagKey) && tags[queryTagKey] == queryTagValue
                            } else {
                                tags.containsKey(queryTagKey)
                            }
                        }
                    }
                } ?: return@mapNotNull null

                val name = tags[OSM_TAG_NAME]
                val lat = element.lat
                val lon = element.lon
                val bounds = element.bounds
                val location = if (lat != null && lon != null) {
                    Location(lat, lon)
                } else {
                    bounds?.center()
                } ?: return@mapNotNull null

                Place(
                    osmId = element.id.toString(),
                    name = name?.toMessage() ?: placeCategory.title,
                    placeType = when (element.type) {
                        ElementType.RELATION -> PlaceType.RELATION
                        ElementType.WAY -> PlaceType.WAY
                        ElementType.NODE -> PlaceType.NODE
                    },
                    location = location,
                    fullAddress = LocationFormatter.formatString(location),
                    placeFeature = PlaceFeature.MAP_SEARCH,
                    placeCategory = placeCategory,
                    osmTags = tags
                )
            }
            .sortedByDescending { it.placeType.ordinal }
            .sortedBy { it.osmTags?.size }
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
