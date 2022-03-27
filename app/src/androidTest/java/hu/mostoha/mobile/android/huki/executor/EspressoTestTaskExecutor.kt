package hu.mostoha.mobile.android.huki.executor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import javax.inject.Inject

@ExperimentalCoroutinesApi
class EspressoTestTaskExecutor @Inject constructor() : TaskExecutor {

    override fun runOnUi(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        runTest {
            block.invoke()
        }
    }

    override fun runOnUiCancellable(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job {
        runTest {
            block.invoke()
        }
        return Job().apply {
            complete()
        }
    }

}
