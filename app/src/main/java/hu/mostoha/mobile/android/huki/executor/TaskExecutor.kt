package hu.mostoha.mobile.android.huki.executor

import hu.mostoha.mobile.android.huki.interactor.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface TaskExecutor {

    suspend fun <T> runOnBackground(block: suspend () -> TaskResult<T>): TaskResult<T>

    fun runOnUi(coroutineScope: CoroutineScope, block: suspend () -> Unit)

    fun runOnUiCancellable(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job

}