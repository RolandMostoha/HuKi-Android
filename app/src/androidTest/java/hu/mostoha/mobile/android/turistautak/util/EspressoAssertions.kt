package hu.mostoha.mobile.android.turistautak.util

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

object EspressoAssertions {

    fun checkDisplayed(@StringRes res: Int) {
        onView(ViewMatchers.withText(res)).check(matches(isDisplayed()))
    }

}