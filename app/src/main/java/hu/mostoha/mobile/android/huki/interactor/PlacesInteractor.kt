package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlacesInteractor @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val placesRepository: PlacesRepository
) {

    fun requestGetPlacesByFlow(searchText: String, location: Location? = null): Flow<List<Place>> {
        return transformRequestToFlow(
            request = { placesRepository.getPlacesBy(searchText, location) },
            exceptionLogger = exceptionLogger
        )
    }

    fun requestGeometryFlow(osmId: String, placeType: PlaceType): Flow<Geometry> {
        return transformRequestToFlow(
            request = { placesRepository.getGeometry(osmId, placeType) },
            exceptionLogger = exceptionLogger
        )
    }

    fun requestGetHikingRoutesFlow(boundingBox: BoundingBox): Flow<List<HikingRoute>> {
        return transformRequestToFlow(
            request = { placesRepository.getHikingRoutes(boundingBox) },
            exceptionLogger = exceptionLogger
        )
    }

}
