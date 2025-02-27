package hu.mostoha.mobile.android.huki.util

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun runTestDefault(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 1.seconds,
    testBody: suspend TestScope.() -> Unit
) {
    runTest(context, timeout, testBody)
}
