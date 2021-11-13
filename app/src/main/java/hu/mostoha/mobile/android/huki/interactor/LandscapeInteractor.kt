package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import timber.log.Timber
import javax.inject.Inject

class LandscapeInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val landscapeRepository: LandscapeRepository
) {

    suspend fun requestGetLandscapes(): TaskResult<List<Landscape>> {
        return taskExecutor.runOnBackground {
            try {
                val response = landscapeRepository.getLandscapes()

                TaskResult.Success(response)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.error_message_unknown, exception))
            }
        }
    }

}
