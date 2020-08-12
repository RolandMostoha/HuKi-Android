package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.Location
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceDetails
import hu.mostoha.mobile.android.turistautak.model.domain.PlacePrediction
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import hu.mostoha.mobile.android.turistautak.model.network.FeaturesItem
import hu.mostoha.mobile.android.turistautak.model.network.OsmType
import hu.mostoha.mobile.android.turistautak.model.network.OverpassQueryResult
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.PhotonService
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputFormat
import hu.mostoha.mobile.android.turistautak.network.overpasser.output.OutputOrder
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
        val response = when (placeType) {
            PlaceType.NODE -> getNode(id)
            PlaceType.WAY -> getNodesByWay(id)
            PlaceType.RELATION -> TODO()
        }
        val node = response.elements[0]
        return PlaceDetails(
            id = node.id.toString(),
            location = Location(node.lat!!, node.lon!!)
        )
    }

    private suspend fun getNode(id: String): OverpassQueryResult {
        val query = OverpassQuery()
            .format(OutputFormat.JSON)
            .timeout(NetworkConfig.TIMEOUT_IN_SECONDS)
            .filterQuery()
            .nodeBy(id)
            .end()
            .output(OutputVerbosity.BODY, null, OutputOrder.QT, 1)
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
            .output(OutputVerbosity.BODY, null, OutputOrder.QT, -1)
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

}
