package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlacesInteractor @Inject constructor(
    exceptionLogger: ExceptionLogger,
    private val placesRepository: PlacesRepository
) : BaseInteractor(exceptionLogger) {

    suspend fun requestGetPlacesByFlow(searchText: String): Flow<List<Place>> {
        return getRequestFlow { placesRepository.getPlacesBy(searchText) }
    }

    suspend fun requestGeometryFlow(osmId: String, placeType: PlaceType): Flow<Geometry> {
        return getRequestFlow { placesRepository.getGeometry(osmId, placeType) }
    }

    suspend fun requestGetHikingRoutesFlow(boundingBox: BoundingBox): Flow<List<HikingRoute>> {
        return getRequestFlow { placesRepository.getHikingRoutes(boundingBox) }
    }

}
