package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDetailsNetworkDomainMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.huki.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.huki.overpasser.query.OverpassQuery
import timber.log.Timber
import javax.inject.Inject

class OsmPlacesRepository @Inject constructor(
    private val overpassService: OverpassService,
    private val placeDetailsNetworkDomainMapper: PlaceDetailsNetworkDomainMapper
) : PlacesRepository {

    companion object {
        const val OSM_HIKING_ROUTES_QUERY_LIMIT = 30
        const val OSM_PLACE_CATEGORY_QUERY_LIMIT = 100
    }

    override suspend fun getGeometry(osmId: String, placeType: PlaceType): Geometry {
        return when (placeType) {
            PlaceType.NODE -> {
                val response = getNode(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByNode(response, osmId)
            }
            PlaceType.WAY -> {
                val response = getNodesByWay(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByWay(response, osmId)
            }
            PlaceType.RELATION, PlaceType.HIKING_ROUTE -> {
                val response = getNodesByRelation(osmId)

                placeDetailsNetworkDomainMapper.mapGeometryByRelation(response, osmId)
            }
        }
    }

    override suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .rel()
            .tag("type", "route")
            .tag("route", "hiking")
            .tag("jel")
            .boundingBox(boundingBox.south, boundingBox.west, boundingBox.north, boundingBox.east)
            .end()
            .output(OutputVerbosity.TAGS, null, null, OSM_HIKING_ROUTES_QUERY_LIMIT)
            .build()

        val response = overpassService.interpreter(query)

        return placeDetailsNetworkDomainMapper.mapHikingRoutes(response)
    }

    override suspend fun getPlacesByCategories(categories: Set<PlaceCategory>, boundingBox: BoundingBox): List<Place> {
        val osmQueryTags = categories.flatMap { it.osmQueryTags }
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .nwrs(osmQueryTags, boundingBox.south, boundingBox.west, boundingBox.north, boundingBox.east)
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.BB, null, OSM_PLACE_CATEGORY_QUERY_LIMIT)
            .build()

        Timber.d("OSM query: $query")

        val response = overpassService.interpreter(query)

        return placeDetailsNetworkDomainMapper.mapPlacesByCategories(response, categories)
    }

    override suspend fun getOsmTags(osmId: String, placeType: PlaceType): Map<String, String> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .apply {
                when (placeType) {
                    PlaceType.NODE -> nodeBy(osmId)
                    PlaceType.WAY -> wayBy(osmId)
                    else -> relBy(osmId)
                }
            }
            .end()
            .output(OutputVerbosity.TAGS, OutputModificator.BB, null, 1)
            .build()

        return overpassService.interpreter(query).elements.firstOrNull()?.tags ?: emptyMap()
    }

    private suspend fun getNode(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .nodeBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, 1)
            .build()

        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByWay(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .wayBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByRelation(osmId: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .relBy(osmId)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return overpassService.interpreter(query)
    }

}
