package hu.mostoha.mobile.android.turistautak.executor

import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultTaskExecutor @Inject constructor() : TaskExecutor {

    override suspend fun <T> runOnBackground(
        coroutineScope: CoroutineScope,
        block: suspend () -> TaskResult<T>
    ): TaskResult<T> {
        return withContext(Dispatchers.IO) {
            block.invoke()
        }
    }

    override fun runOnMain(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        coroutineScope.launch {
            block.invoke()
        }
    }

}