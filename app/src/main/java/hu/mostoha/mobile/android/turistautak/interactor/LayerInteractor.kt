package hu.mostoha.mobile.android.turistautak.interactor

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class LayerInteractor @Inject constructor(
    private val layerRepository: LayerRepository
) {

    fun requestGetHikingLayer(
        viewModelScope: CoroutineScope,
        onResult: (TaskResult<File?>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val file = layerRepository.getHikingLayerFile()
                onResult(TaskResult.Success(file))
            } catch (exception: Exception) {
                Timber.w(exception)
                onResult(
                    TaskResult.Error(
                        DomainException(R.string.default_error_message_unknown, exception)
                    )
                )
            }
        }
    }

    fun requestDownloadHikingLayer(
        viewModelScope: CoroutineScope,
        onResult: (TaskResult<Long>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestId = layerRepository.downloadHikingLayerFile()
                onResult(TaskResult.Success(requestId))
            } catch (exception: Exception) {
                Timber.w(exception)

                onResult(
                    TaskResult.Error(
                        DomainException(R.string.default_error_message_unknown, exception)
                    )
                )
            }
        }
    }

    fun requestSaveHikingLayer(
        downloadId: Long,
        viewModelScope: CoroutineScope,
        onResult: (TaskResult<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                layerRepository.saveHikingLayerFile(downloadId)
                onResult(TaskResult.Success(Unit))
            } catch (exception: Exception) {
                Timber.w(exception)

                val domainException = when (exception) {
                    is FileNotFoundException -> {
                        DomainException(R.string.download_layer_missing_downloaded_file, exception)
                    }
                    else -> {
                        DomainException(R.string.default_error_message_unknown, exception)
                    }
                }
                onResult(TaskResult.Error(domainException))
            }
        }
    }

}