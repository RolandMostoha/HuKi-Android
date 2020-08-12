package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.PlaceDetails
import hu.mostoha.mobile.android.turistautak.model.domain.PlacePrediction
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType

interface PlacesRepository {
    suspend fun getPlacesBy(searchText: String): List<PlacePrediction>
    suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails
}