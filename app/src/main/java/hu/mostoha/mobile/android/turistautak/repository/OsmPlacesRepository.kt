package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.generator.PlacesDomainModelGenerator
import hu.mostoha.mobile.android.turistautak.model.network.OsmType
import hu.mostoha.mobile.android.turistautak.model.network.OverpassQueryResponse
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.PhotonService
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.turistautak.network.overpasser.query.OverpassQuery
import javax.inject.Inject

class OsmPlacesRepository @Inject constructor(
    private val photonService: PhotonService,
    private val overpassService: OverpassService,
    private val modelGenerator: PlacesDomainModelGenerator
) : PlacesRepository {

    override suspend fun getPlacesBy(searchText: String): List<PlacePrediction> {
        val response = photonService.query(searchText, 10)
        val nodesAndWays = response.copy(
            features = response.features.filter { it.properties.osmType != OsmType.RELATION }
        )
        return modelGenerator.generatePlacePredictions(nodesAndWays)
    }

    override suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails {
        return when (placeType) {
            PlaceType.NODE -> {
                val response = getNode(id)
                modelGenerator.generatePlaceDetailsByNode(response)
            }
            PlaceType.WAY -> {
                val response = getNodesByWay(id)
                modelGenerator.generatePlaceDetailsByWay(response, id)
            }
            PlaceType.RELATION -> {
                val response = getNodesByRelation(id)
                modelGenerator.generatePlaceDetailsByRel(response, id)
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
            .output(OutputVerbosity.TAGS, null, null, 50)
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
            .output(OutputVerbosity.BODY, null, null, 1)
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
            .output(OutputVerbosity.GEOM, null, null, -1)
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
            .output(OutputVerbosity.GEOM, null, null, -1)
            .build()
        return overpassService.interpreter(query)
    }

}
