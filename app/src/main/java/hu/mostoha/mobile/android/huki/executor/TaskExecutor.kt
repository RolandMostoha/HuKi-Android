package hu.mostoha.mobile.android.huki.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface TaskExecutor {

    fun runOnUi(coroutineScope: CoroutineScope, block: suspend () -> Unit)

    fun runOnUiCancellable(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job

}
