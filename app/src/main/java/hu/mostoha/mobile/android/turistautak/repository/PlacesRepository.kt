package hu.mostoha.mobile.android.turistautak.repository

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place

interface PlacesRepository {
    suspend fun getPlacesBy(searchText: String): List<AutocompletePrediction>
    suspend fun getPlaceDetails(placeId: String): Place
}