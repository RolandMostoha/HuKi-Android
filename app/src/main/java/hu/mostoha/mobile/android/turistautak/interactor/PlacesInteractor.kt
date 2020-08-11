package hu.mostoha.mobile.android.turistautak.interactor

import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import timber.log.Timber
import javax.inject.Inject

class PlacesInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val placesRepository: PlacesRepository
) {

    suspend fun requestGetPlacesBy(searchText: String): TaskResult<List<AutocompletePrediction>> {
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

    suspend fun requestGetGetPlaceDetails(placeId: String): TaskResult<Place> {
        return taskExecutor.runOnBackground {
            try {
                val response = placesRepository.getPlaceDetails(placeId)

                TaskResult.Success(response)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.default_error_message_unknown, exception))
            }
        }
    }

}
