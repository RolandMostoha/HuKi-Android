package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.ErrorOccurred
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.LayerLoading
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    private val layerInteractor: LayerInteractor
) : BaseViewModel<HomeLiveEvents, HomeViewState>() {

    fun checkHikingLayer() {
        postEvent(LayerLoading(true))

        layerInteractor.requestGetHikingLayer(viewModelScope) { result ->
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
    }

    fun downloadHikingLayer() {
        postEvent(LayerLoading(true))

        layerInteractor.requestDownloadHikingLayer(viewModelScope) { result ->
            when (result) {
                is TaskResult.Error -> {
                    postEvent(LayerLoading(false))
                    postEvent(ErrorOccurred(result.domainException.messageRes))
                }
            }
        }
    }

    fun handleFileDownloaded(downloadId: Long) {
        postEvent(LayerLoading(true))

        layerInteractor.requestSaveHikingLayer(downloadId, viewModelScope) { result ->
            when (result) {
                is TaskResult.Success -> {
                    checkHikingLayer()
                }
                is TaskResult.Error -> {
                    postEvent(ErrorOccurred(result.domainException.messageRes))
                }
            }
        }
    }

}

data class HomeViewState(
    val hikingLayerFile: File?
) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(@StringRes val messageRes: Int) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
}
