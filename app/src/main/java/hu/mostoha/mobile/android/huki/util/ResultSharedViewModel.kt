package hu.mostoha.mobile.android.huki.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class ResultSharedViewModel<T> : ViewModel() {

    private val _result = MutableStateFlow<T?>(null)
    val result: StateFlow<T?>
        get() = _result.asStateFlow()

    fun updateResult(result: T) {
        _result.value = result
    }

    fun clearResult() {
        _result.value = null
    }

}
