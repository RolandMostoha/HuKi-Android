package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.*

interface PlacesRepository {

    suspend fun getPlacesBy(searchText: String): List<Place>

    suspend fun getPlaceDetails(osmId: String, placeType: PlaceType): PlaceDetails

    suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute>

}
