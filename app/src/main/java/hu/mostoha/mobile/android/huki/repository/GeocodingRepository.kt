package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place

const val GEOCODING_SEARCH_QUERY_LIMIT = 20

interface GeocodingRepository {

    suspend fun getPlacesBy(searchText: String, location: Location? = null): List<Place>

    suspend fun getPlace(location: Location): Place?

}
