package hu.mostoha.mobile.android.huki.executor

import hu.mostoha.mobile.android.huki.interactor.TaskResult
import kotlinx.coroutines.*
import javax.inject.Inject

class DefaultTaskExecutor @Inject constructor() : TaskExecutor {

    override suspend fun <T> runOnBackground(block: suspend () -> TaskResult<T>): TaskResult<T> {
        return withContext(Dispatchers.IO) {
            block.invoke()
        }
    }

    override fun runOnUi(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        coroutineScope.launch {
            block.invoke()
        }
    }

    override fun runOnUiCancellable(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job {
        return coroutineScope.launch {
            block.invoke()
        }
    }

}
