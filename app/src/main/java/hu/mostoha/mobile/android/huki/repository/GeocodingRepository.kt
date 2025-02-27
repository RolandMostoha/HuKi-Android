package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile

interface GeocodingRepository {

    suspend fun getAutocompletePlaces(searchText: String, boundingBox: BoundingBox): List<PlaceProfile>

    suspend fun getPlaceProfile(location: Location): PlaceProfile?

}
