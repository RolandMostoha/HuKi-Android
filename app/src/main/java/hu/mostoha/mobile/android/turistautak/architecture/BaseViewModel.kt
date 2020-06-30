package hu.mostoha.mobile.android.turistautak.architecture

import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent

open class BaseViewModel<T : LiveEvents> : ViewModel() {

    val liveEvents = LiveEvent<T>()

    fun postEvent(event: T) {
        liveEvents.value = event
    }

}

interface LiveEvents