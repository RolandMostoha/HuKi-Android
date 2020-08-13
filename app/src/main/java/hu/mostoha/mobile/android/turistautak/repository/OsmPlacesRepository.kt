package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.network.*
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.PhotonService
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputVerbosity
import hu.mostoha.mobile.android.turistautak.network.overpasser.query.OverpassQuery
import javax.inject.Inject

class OsmPlacesRepository @Inject constructor(
    private val photonService: PhotonService,
    private val overpassService: OverpassService
) : PlacesRepository {

    override suspend fun getPlacesBy(searchText: String): List<PlacePrediction> {
        val response = photonService.query(searchText, 10)
        return response.features.map {
            it.toPlacePrediction()
        }
    }

    override suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails {
        when (placeType) {
            PlaceType.NODE -> {
                val nodeElement = getNode(id).elements.first()
                return PlaceDetails(
                    id = nodeElement.id.toString(),
                    payLoad = PayLoad.Node(Location(nodeElement.lat!!, nodeElement.lon!!))
                )
            }
            PlaceType.WAY -> {
                val queryResult = getNodesByWay(id)
                val wayElement = queryResult.elements.firstOrNull { ElementType.WAY == it.type } ?: TODO()
                val locations = wayElement.extractLocations()
                return PlaceDetails(
                    id = wayElement.id.toString(),
                    payLoad = PayLoad.Way(locations)
                )
            }
            PlaceType.RELATION -> {
                val queryResult = getNodesByRelation(id)
                val relElement = queryResult.elements.firstOrNull { ElementType.RELATION == it.type } ?: TODO()
                val locations = relElement.extractLocations()
                return PlaceDetails(
                    id = relElement.id.toString(),
                    payLoad = PayLoad.Way(locations)
                )
            }
        }
    }

    private suspend fun getNode(id: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .nodeBy(id)
            .end()
            .output(OutputVerbosity.BODY, null, null, 1)
            .build()
        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByWay(id: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .wayBy(id)
            .nodeBy("w")
            .end()
            .output(OutputVerbosity.GEOM, null, null, -1)
            .build()
        return overpassService.interpreter(query)
    }

    private suspend fun getNodesByRelation(id: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .relBy(id)
            .wayBy("r")
            .nodeBy("w")
            .end()
            .output(OutputVerbosity.GEOM, null, null, -1)
            .build()
        return overpassService.interpreter(query)
    }

    private fun FeaturesItem.toPlacePrediction(): PlacePrediction {
        val secondaryText = properties.city?.let { city ->
            "${properties.postcode ?: ""} $city"
        }
        return PlacePrediction(
            id = properties.osmId.toString(),
            placeType = properties.osmType.toPlaceType(),
            primaryText = properties.name ?: properties.city ?: properties.osmId.toString(),
            secondaryText = secondaryText
        )

    }

    private fun OsmType.toPlaceType(): PlaceType {
        return when (this) {
            OsmType.RELATION -> PlaceType.RELATION
            OsmType.WAY -> PlaceType.WAY
            OsmType.NODE -> PlaceType.NODE
        }
    }

    private fun Element.extractLocations(): List<Location> {
        return geometry?.mapNotNull {
            val lat = it.lat
            val lon = it.lon
            if (lat != null && lon != null) {
                Location(lat, lon)
            } else {
                null
            }
        } ?: emptyList()
    }

}
