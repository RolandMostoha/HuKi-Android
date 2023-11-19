package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature

const val GEOCODING_SEARCH_QUERY_LIMIT = 20

interface GeocodingRepository {

    suspend fun getPlacesBy(searchText: String, placeFeature: PlaceFeature, location: Location?): List<Place>

    suspend fun getPlace(location: Location, placeFeature: PlaceFeature): Place?

}
