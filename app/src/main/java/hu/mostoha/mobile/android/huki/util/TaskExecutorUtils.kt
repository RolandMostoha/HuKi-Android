package hu.mostoha.mobile.android.huki.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import kotlinx.coroutines.Job

fun ViewModel.launch(taskExecutor: TaskExecutor, block: suspend () -> Unit) {
    taskExecutor.runOnUi(viewModelScope, block)
}

fun ViewModel.launchCancellable(taskExecutor: TaskExecutor, block: suspend () -> Unit): Job {
    return taskExecutor.runOnUiCancellable(viewModelScope, block)
}
