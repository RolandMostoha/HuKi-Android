package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.model.domain.DownloadState
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.ErrorEvent
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.LayerLoadingEvent
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    private val layerInteractor: LayerInteractor
) : BaseViewModel<HomeLiveEvents>() {

    val hikingLayerFile = MutableLiveData<File?>()

    fun checkForHikingLayer() {
        postEvent(LayerLoadingEvent(true))

        layerInteractor.requestHikingLayer(viewModelScope) { result ->
            postEvent(LayerLoadingEvent(false))

            when (result) {
                is TaskResult.Success -> {
                    hikingLayerFile.value = result.data
                }
                is TaskResult.Error -> {
                    postEvent(ErrorEvent(result.domainException.messageRes))
                }
            }
        }
    }

    fun downloadHikingLayer() {
        postEvent(LayerLoadingEvent(true))

        layerInteractor.requestDownloadHikingLayer(viewModelScope) { result ->
            postEvent(LayerLoadingEvent(false))

            when (result) {
                is TaskResult.Success -> {

                }
                is TaskResult.Error -> {
                    postEvent(ErrorEvent(result.domainException.messageRes))
                }
            }
        }
    }

}

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorEvent(@StringRes val messageRes: Int) : HomeLiveEvents()
    data class LayerLoadingEvent(val isInProgress: Boolean) : HomeLiveEvents()
    data class LayerDownloadingEvent(val state: DownloadState) : HomeLiveEvents()
}
