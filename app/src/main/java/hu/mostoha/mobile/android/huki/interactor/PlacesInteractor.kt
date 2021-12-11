package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import javax.inject.Inject

class PlacesInteractor @Inject constructor(
    taskExecutor: TaskExecutor,
    private val placesRepository: PlacesRepository
) : BaseInteractor(taskExecutor) {

    suspend fun requestGetPlacesBy(searchText: String): TaskResult<List<Place>> {
        return processRequest(request = { placesRepository.getPlacesBy(searchText) })
    }

    suspend fun requestGetGeometry(osmId: String, placeType: PlaceType): TaskResult<Geometry> {
        return processRequest(request = { placesRepository.getGeometry(osmId, placeType) })
    }

    suspend fun requestGetHikingRoutes(boundingBox: BoundingBox): TaskResult<List<HikingRoute>> {
        return processRequest(request = { placesRepository.getHikingRoutes(boundingBox) })
    }

}
