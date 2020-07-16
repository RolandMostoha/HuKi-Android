package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.ErrorOccurred
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.LayerLoading
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    taskExecutor: TaskExecutor,
    private val layerInteractor: LayerInteractor
) : BaseViewModel<HomeLiveEvents, HomeViewState>(taskExecutor) {

    fun checkHikingLayer() = launch {
        postEvent(LayerLoading(true))

        val result = layerInteractor.requestGetHikingLayer(viewModelScope)

        postEvent(LayerLoading(false))

        when (result) {
            is TaskResult.Success -> {
                postState(HomeViewState(result.data))
            }
            is TaskResult.Error -> {
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }
    }

    fun downloadHikingLayer() = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestDownloadHikingLayer(viewModelScope)) {
            is TaskResult.Error -> {
                postEvent(LayerLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }
    }

    fun handleFileDownloaded(downloadId: Long) = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestSaveHikingLayer(downloadId, viewModelScope)) {
            is TaskResult.Success -> {
                checkHikingLayer()
            }
            is TaskResult.Error -> {
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }

    }

}

data class HomeViewState(val hikingLayerFile: File?) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(@StringRes val messageRes: Int) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
}
