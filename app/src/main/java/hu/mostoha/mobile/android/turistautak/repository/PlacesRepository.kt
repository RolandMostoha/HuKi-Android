package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.PlaceDetails
import hu.mostoha.mobile.android.turistautak.model.domain.PlacePrediction

interface PlacesRepository {
    suspend fun getPlacesBy(searchText: String): List<PlacePrediction>
    suspend fun getPlaceDetails(placeId: String): PlaceDetails
}