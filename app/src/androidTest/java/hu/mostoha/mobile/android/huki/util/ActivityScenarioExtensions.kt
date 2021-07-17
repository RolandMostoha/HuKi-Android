package hu.mostoha.mobile.android.huki.util

import android.app.Activity
import androidx.test.core.app.launchActivity

inline fun <reified T : Activity> launch(then: () -> Unit) {
    val scenario = launchActivity<T>()
    then.invoke()
    scenario.close()
}