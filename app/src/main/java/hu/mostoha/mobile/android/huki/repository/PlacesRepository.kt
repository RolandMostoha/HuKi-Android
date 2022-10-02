package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType

interface PlacesRepository {

    suspend fun getPlacesBy(searchText: String, location: Location? = null): List<Place>

    suspend fun getGeometry(osmId: String, placeType: PlaceType): Geometry

    suspend fun getHikingRoutes(boundingBox: BoundingBox): List<HikingRoute>

}
