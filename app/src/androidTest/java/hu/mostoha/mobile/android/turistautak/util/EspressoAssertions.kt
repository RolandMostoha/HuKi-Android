package hu.mostoha.mobile.android.turistautak.util

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

object EspressoAssertions {

    fun checkTextDisplayed(@StringRes res: Int) {
        onView(withText(res)).check(matches(isDisplayed()))
    }

    fun checkViewDisplayed(@IdRes res: Int) {
        onView(withText(res)).check(matches(isDisplayed()))
    }

}