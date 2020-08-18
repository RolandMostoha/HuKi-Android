package hu.mostoha.mobile.android.turistautak.model.generator

import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.model.network.*
import javax.inject.Inject

class PlacesDomainModelGenerator @Inject constructor() {

    fun generatePlacePredictions(response: PhotonQueryResponse): List<PlacePrediction> {
        return response.features.map {
            PlacePrediction(
                id = it.properties.osmId.toString(),
                placeType = when (it.properties.osmType) {
                    OsmType.RELATION -> PlaceType.RELATION
                    OsmType.WAY -> PlaceType.WAY
                    OsmType.NODE -> PlaceType.NODE
                },
                primaryText = it.properties.name ?: it.properties.city ?: it.properties.osmId.toString(),
                secondaryText = it.properties.city?.let { city ->
                    "${it.properties.postcode ?: ""} $city"
                }
            )
        }
    }

    fun generatePlaceDetailsByNode(response: OverpassQueryResponse): PlaceDetails {
        val nodeElement = response.elements.first()
        return PlaceDetails(
            id = nodeElement.id.toString(),
            payLoad = PayLoad.Node(Location(nodeElement.lat!!, nodeElement.lon!!))
        )
    }

    fun generatePlaceDetailsByRel(response: OverpassQueryResponse, id: String): PlaceDetails {
        val relElement = response.elements.firstOrNull {
            it.type == ElementType.RELATION && it.id.toString() == id
        } ?: TODO()
        val locations = relElement.extractLocations()
        return PlaceDetails(
            id = relElement.id.toString(),
            payLoad = PayLoad.Way(locations)
        )
    }

    fun generatePlaceDetailsByWay(response: OverpassQueryResponse, id: String): PlaceDetails {
        val wayElement = response.elements.firstOrNull {
            it.type == ElementType.WAY && it.id.toString() == id
        } ?: TODO()
        val locations = wayElement.extractLocations()
        return PlaceDetails(
            id = wayElement.id.toString(),
            payLoad = PayLoad.Way(locations)
        )
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
