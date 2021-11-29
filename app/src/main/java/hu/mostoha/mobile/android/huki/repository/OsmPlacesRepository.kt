package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.generator.PlacesDomainModelGenerator
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.network.PhotonService
import hu.mostoha.mobile.android.huki.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.huki.overpasser.output.OutputModificator
import hu.mostoha.mobile.android.huki.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.huki.overpasser.query.OverpassQuery
import javax.inject.Inject

class OsmPlacesRepository @Inject constructor(
    private val photonService: PhotonService,
    private val overpassService: OverpassService,
    private val modelGenerator: PlacesDomainModelGenerator
) : PlacesRepository {

    companion object {
        const val PHOTON_SEARCH_QUERY_LIMIT = 20
        const val OSM_HIKING_ROUTES_QUERY_LIMIT = 50
    }

    override suspend fun getPlacesBy(searchText: String): List<Place> {
        val response = photonService.query(searchText, PHOTON_SEARCH_QUERY_LIMIT)

        return modelGenerator.generatePlace(response)
    }

    override suspend fun getGeometry(osmId: String, placeType: PlaceType): Geometry {
        return when (placeType) {
            PlaceType.NODE -> {
                val response = getNode(osmId)

                modelGenerator.generateGeometryByNode(response, osmId)
            }
            PlaceType.WAY -> {
                val response = getNodesByWay(osmId)

                modelGenerator.generateGeometryByWay(response, osmId)
            }
            PlaceType.RELATION -> {
                val response = getNodesByRelation(osmId)

                modelGenerator.generateGeometryByRelation(response, osmId)
            }
        }
    }

    override suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute> {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_SEC)
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

        return modelGenerator.generateHikingRoutes(response)
    }

    private suspend fun getNode(id: String): OverpassQueryResponse {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_SEC)
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
            .timeout(NetworkConfig.TIMEOUT_SEC)
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
            .timeout(NetworkConfig.TIMEOUT_SEC)
            .filterQuery()
            .relBy(id)
            .end()
            .output(OutputVerbosity.BODY, OutputModificator.GEOM, null, -1)
            .build()

        return overpassService.interpreter(query)
    }

}
