package hu.mostoha.mobile.android.turistautak.executor

import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalCoroutinesApi
class EspressoTestTaskExecutor @Inject constructor() : TaskExecutor {

    override suspend fun <T> runOnBackground(
        coroutineScope: CoroutineScope,
        block: suspend () -> TaskResult<T>
    ): TaskResult<T> {
        return withContext(TestCoroutineDispatcher()) {
            block.invoke()
        }
    }

    override fun runOnMain(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        runBlockingTest {
            block.invoke()
        }
    }

}