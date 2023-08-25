package hu.mostoha.mobile.android.huki.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

open class EventSharedViewModel<T> : ViewModel() {

    private val _event = MutableSharedFlow<T>()
    val event: SharedFlow<T>
        get() = _event.asSharedFlow()

    fun updateEvent(result: T) {
        viewModelScope.launch {
            _event.emit(result)
        }
    }

}
