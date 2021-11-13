package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class LayerInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val layerRepository: HikingLayerRepository
) {

    suspend fun requestGetHikingLayer(): TaskResult<File?> {
        return taskExecutor.runOnBackground {
            try {
                val file = layerRepository.getHikingLayerFile()

                TaskResult.Success(file)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.error_message_unknown, exception))
            }
        }
    }

    suspend fun requestDownloadHikingLayer(): TaskResult<Long> {
        return taskExecutor.runOnBackground {
            try {
                val requestId = layerRepository.downloadHikingLayerFile()

                TaskResult.Success(requestId)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(DomainException(R.string.error_message_unknown, exception))
            }
        }
    }

    suspend fun requestSaveHikingLayer(downloadId: Long): TaskResult<Unit> {
        return taskExecutor.runOnBackground {
            try {
                layerRepository.saveHikingLayerFile(downloadId)

                TaskResult.Success(Unit)
            } catch (exception: Exception) {
                Timber.w(exception)

                TaskResult.Error(
                    when (exception) {
                        is FileNotFoundException -> {
                            DomainException(R.string.download_layer_missing_downloaded_file, exception)
                        }
                        else -> {
                            DomainException(R.string.error_message_unknown, exception)
                        }
                    }
                )
            }
        }
    }

}
