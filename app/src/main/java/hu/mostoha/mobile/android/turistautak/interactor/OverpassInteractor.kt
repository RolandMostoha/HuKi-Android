package hu.mostoha.mobile.android.turistautak.interactor

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.network.model.OverpassQueryResult
import hu.mostoha.mobile.android.turistautak.repository.OverpassRepository
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject

class OverpassInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val overpassRepository: OverpassRepository
) {

    suspend fun requestSearchHikingRelationsBy(
        searchText: String,
        coroutineScope: CoroutineScope
    ): TaskResult<OverpassQueryResult> {
        return taskExecutor.runOnBackground(coroutineScope) {
            try {
                val file = overpassRepository.searchHikingRelationsBy(searchText)

                TaskResult.Success(file)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.default_error_message_unknown, exception))
            }
        }
    }

}
