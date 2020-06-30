package hu.mostoha.mobile.android.turistautak.interactor

import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.model.domain.DownloadState
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class LayerInteractor @Inject constructor(
    private val layerRepository: LayerRepository
) {

    fun requestHikingLayer(viewModelScope: CoroutineScope, onResult: (TaskResult<File?>) -> Unit) {
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
        onResult: (TaskResult<DownloadState>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val requestId = layerRepository.downloadHikingLayerFile()
                onResult(TaskResult.Success(DownloadState.Started(requestId)))
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

}