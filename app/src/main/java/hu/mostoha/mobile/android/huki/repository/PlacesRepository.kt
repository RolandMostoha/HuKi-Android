package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.*

interface PlacesRepository {

    suspend fun getPlacesBy(searchText: String): List<PlacePrediction>

    suspend fun getPlaceDetails(id: String, placeType: PlaceType): PlaceDetails

    suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute>

}