package hu.mostoha.mobile.android.huki.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry

internal val testAppContext: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

internal val testContext: Context
    get() = InstrumentationRegistry.getInstrumentation().context
