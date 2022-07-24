package hu.mostoha.mobile.android.huki.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun runTestDefault(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatchTimeoutMs: Long = 1000,
    testBody: suspend TestScope.() -> Unit
): TestResult {
    return runTest(context, dispatchTimeoutMs, testBody)
}
