package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    private val layerInteractor: LayerInteractor
) : ViewModel() {

    val errorEvent = LiveEvent<Int>()

    val hikingLayerLoadingEvent = LiveEvent<Boolean>()
    val hikingLayer = MutableLiveData<File?>()

    fun checkForHikingLayer() {
        hikingLayerLoadingEvent.value = true

        layerInteractor.requestHikingLayer(viewModelScope) { result ->
            hikingLayerLoadingEvent.value = false

            when (result) {
                is TaskResult.Success -> {
                    hikingLayer.value = result.data
                }
                is TaskResult.Error -> {
                    errorEvent.value = result.domainException.messageRes
                }
            }
        }
    }

}
