package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import java.io.File
import javax.inject.Inject

class HikingLayerInteractor @Inject constructor(
    taskExecutor: TaskExecutor,
    exceptionLogger: ExceptionLogger,
    private val hikingLayerRepository: HikingLayerRepository
) : BaseInteractor(taskExecutor, exceptionLogger) {

    suspend fun requestGetHikingLayerFile(): TaskResult<File?> {
        return processRequest { hikingLayerRepository.getHikingLayerFile() }
    }

    suspend fun requestDownloadHikingLayerFile(): TaskResult<Long> {
        return processRequest { hikingLayerRepository.downloadHikingLayerFile() }
    }

    suspend fun requestSaveHikingLayerFile(downloadId: Long): TaskResult<Unit> {
        return processRequest { hikingLayerRepository.saveHikingLayerFile(downloadId) }
    }

}
