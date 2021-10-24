package hu.mostoha.mobile.android.huki.executor

import hu.mostoha.mobile.android.huki.interactor.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
class TestTaskExecutor : TaskExecutor {

    override suspend fun <T> runOnBackground(block: suspend () -> TaskResult<T>): TaskResult<T> {
        return withContext(TestCoroutineDispatcher()) {
            block.invoke()
        }
    }

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
