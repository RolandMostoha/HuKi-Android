package hu.mostoha.mobile.android.huki.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultTaskExecutor @Inject constructor() : TaskExecutor {

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
