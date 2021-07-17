package hu.mostoha.mobile.android.huki.architecture

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadilq.liveevent.LiveEvent
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import kotlinx.coroutines.Job

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
        taskExecutor.runOnUi(viewModelScope, block)
    }

    fun launchCancellable(block: suspend () -> Unit): Job {
        return taskExecutor.runOnUiCancellable(viewModelScope, block)
    }

}

interface LiveEvents

interface ViewState