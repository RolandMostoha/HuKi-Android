package hu.mostoha.mobile.android.turistautak.util

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

object EspressoAssertions {

    fun checkTextDisplayed(@StringRes res: Int) {
        onView(withText(res)).check(matches(isDisplayed()))
    }

    fun checkViewDisplayed(@IdRes id: Int) {
        onView(withId(id)).check(matches(isDisplayed()))
    }

    fun clickViewWithId(@IdRes id: Int) {
        onView(withId(id)).perform(click())
    }

    fun clickViewWithText(@StringRes res: Int) {
        onView(withText(res)).perform(click())
    }

}