package hu.mostoha.mobile.android.turistautak.architecture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor

open class BaseViewModel<E : LiveEvents, VS : ViewState>(
    private val taskExecutor: TaskExecutor
) : ViewModel() {

    val viewState = MutableLiveData<VS>()

    val liveEvents = LiveEvent<E>()

    fun postEvent(event: E) {
        liveEvents.value = event
    }

    fun postState(state: VS) {
        viewState.value = state
    }

    fun launch(block: suspend () -> Unit) {
        taskExecutor.runOnMain(viewModelScope, block)
    }

}

interface LiveEvents

interface ViewState