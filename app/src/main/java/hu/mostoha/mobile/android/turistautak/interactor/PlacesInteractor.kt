package hu.mostoha.mobile.android.turistautak.interactor

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import timber.log.Timber
import javax.inject.Inject

class PlacesInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val placesRepository: PlacesRepository
) {

    suspend fun requestGetPlacesBy(searchText: String): TaskResult<List<PlacePrediction>> {
        return taskExecutor.runOnBackground {
            try {
                val response = placesRepository.getPlacesBy(searchText)

                TaskResult.Success(response)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.default_error_message_unknown, exception))
            }
        }
    }

    suspend fun requestGetPlaceDetails(id: String, placeType: PlaceType): TaskResult<PlaceDetails> {
        return taskExecutor.runOnBackground {
            try {
                val response = placesRepository.getPlaceDetails(id, placeType)

                TaskResult.Success(response)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.default_error_message_unknown, exception))
            }
        }
    }

    suspend fun requestGetHikingRoutes(boundingBox: BoundingBox): TaskResult<List<HikingRoute>> {
        return taskExecutor.runOnBackground {
            try {
                val response = placesRepository.getHikingRoutes(boundingBox)

                TaskResult.Success(response)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.default_error_message_unknown, exception))
            }
        }
    }

}
