package hu.mostoha.mobile.android.turistautak.repository

import hu.mostoha.mobile.android.turistautak.model.domain.*

interface PlacesRepository {
    suspend fun getPlacesBy(searchText: String): List<PlacePrediction>
    suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails
    suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute>
}