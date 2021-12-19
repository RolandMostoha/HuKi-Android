package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class HikingLayerInteractor @Inject constructor(
    taskExecutor: TaskExecutor,
    private val hikingLayerRepository: HikingLayerRepository
) : BaseInteractor(taskExecutor) {

    suspend fun requestGetHikingLayerFile(): TaskResult<File?> {
        return processRequest(request = { hikingLayerRepository.getHikingLayerFile() })
    }

    suspend fun requestDownloadHikingLayerFile(): TaskResult<Long> {
        return processRequest(request = { hikingLayerRepository.downloadHikingLayerFile() })
    }

    suspend fun requestSaveHikingLayerFile(downloadId: Long): TaskResult<Unit> {
        return processRequest(
            request = { hikingLayerRepository.saveHikingLayerFile(downloadId) },
            domainExceptionMapper = { exception ->
                if (exception is FileNotFoundException) {
                    DomainException(exception, R.string.download_layer_missing_downloaded_file.toMessage())
                } else {
                    GeneralDomainExceptionMapper.map(exception)
                }
            }
        )
    }

}
