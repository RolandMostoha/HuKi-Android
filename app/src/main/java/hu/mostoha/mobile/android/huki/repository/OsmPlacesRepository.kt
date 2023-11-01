package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.mapper.PlacesDomainModelMapper
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.huki.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.huki.overpasser.query.OverpassQuery
import javax.inject.Inject

class OsmPlacesRepository @Inject constructor(
    private val overpassService: OverpassService,
    private val placesDomainModelMapper: PlacesDomainModelMapper
) : PlacesRepository {

    companion object {
        const val OSM_HIKING_ROUTES_QUERY_LIMIT = 30
    }

    override suspend fun getGeometry(osmId: String, placeType: PlaceType): Geometry {
        return when (placeType) {
            PlaceType.NODE -> {
                val response = getNode(osmId)

                placesDomainModelMapper.mapGeometryByNode(response, osmId)
            }
            PlaceType.WAY -> {
                val response = getNodesByWay(osmId)

                placesDomainModelMapper.mapGeometryByWay(response, osmId)
            }
            PlaceType.RELATION, PlaceType.HIKING_ROUTE -> {
                val response = getNodesByRelation(osmId)

                placesDomainModelMapper.mapGeometryByRelation(response, osmId)
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

        return placesDomainModelMapper.mapHikingRoutes(response)
    }

    private suspend fun getNode(id: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .nodeBy(id)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, 1)
            .build()

        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByWay(id: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .wayBy(id)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByRelation(id: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.DEFAULT_TIMEOUT_S)
            .filterQuery()
            .relBy(id)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return overpassService.interpreter(query)
    }

}
