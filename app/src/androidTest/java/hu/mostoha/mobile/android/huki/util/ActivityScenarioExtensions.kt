package hu.mostoha.mobile.android.huki.util

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity

inline fun <reified T : Activity> launch(then: (ActivityScenario<T>) -> Unit) {
    val scenario = launchActivity<T>()
    then.invoke(scenario)
    scenario.close()
}
