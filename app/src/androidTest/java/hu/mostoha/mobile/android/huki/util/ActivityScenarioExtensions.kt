package hu.mostoha.mobile.android.huki.util

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity

inline fun <reified T : Activity> launchScenario(
    intent: Intent? = null,
    then: ActivityScenario<T>.(ActivityScenario<T>) -> Unit
) {
    val scenario = launchActivity<T>(intent)
    then.invoke(scenario, scenario)
    scenario.close()
}
