package hu.mostoha.mobile.android.turistautak.architecture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

open class BaseViewModel<E : LiveEvents, S : ViewState> : ViewModel() {

    val viewState = MutableLiveData<S>()

    val liveEvents = LiveEvent<E>()

    fun postEvent(event: E) {
        liveEvents.value = event
    }

    fun postState(state: S) {
        viewState.value = state
    }

}

interface LiveEvents

interface ViewState