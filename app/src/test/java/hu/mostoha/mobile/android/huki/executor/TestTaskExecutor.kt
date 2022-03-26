package hu.mostoha.mobile.android.huki.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runBlockingTest

@ExperimentalCoroutinesApi
class TestTaskExecutor : TaskExecutor {

    override fun runOnUi(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        runBlockingTest {
            block.invoke()
        }
    }

    override fun runOnUiCancellable(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job {
        runBlockingTest {
            block.invoke()
        }
        return Job().apply {
            complete()
        }
    }

}
