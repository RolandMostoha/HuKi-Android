package hu.mostoha.mobile.android.huki.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class EventSharedViewModel<T> : ViewModel() {

    private val _event = MutableSharedFlow<T>()
    val event: SharedFlow<T>
        get() = _event.asSharedFlow()

    suspend fun updateEvent(result: T) {
        _event.emit(result)
    }

}
