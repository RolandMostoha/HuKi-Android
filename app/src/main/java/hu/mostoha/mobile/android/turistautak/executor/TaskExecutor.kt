package hu.mostoha.mobile.android.turistautak.executor

import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import kotlinx.coroutines.CoroutineScope

interface TaskExecutor {
    suspend fun <T> runOnBackground(coroutineScope: CoroutineScope, block: suspend () -> TaskResult<T>): TaskResult<T>
    fun runOnMain(coroutineScope: CoroutineScope, block: suspend () -> Unit)
}