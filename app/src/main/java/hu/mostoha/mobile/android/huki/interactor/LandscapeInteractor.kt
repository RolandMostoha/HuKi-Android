package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import javax.inject.Inject

class LandscapeInteractor @Inject constructor(
    taskExecutor: TaskExecutor,
    private val landscapeRepository: LandscapeRepository
) : BaseInteractor(taskExecutor) {

    suspend fun requestGetLandscapes(): TaskResult<List<Landscape>> {
        return processRequest(request = { landscapeRepository.getLandscapes() })
    }

}
