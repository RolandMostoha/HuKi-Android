package hu.mostoha.mobile.android.turistautak.util.espresso

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher

fun @receiver:IdRes Int.isDisplayed() {
    onView(withId(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun @receiver:IdRes Int.isNotDisplayed() {
    onView(withId(this)).check(matches(not(ViewMatchers.isDisplayed())))
}

fun @receiver:IdRes Int.isNotCompletelyDisplayed() {
    onView(withId(this)).check(matches(not(isCompletelyDisplayed())))
}

fun @receiver:IdRes Int.typeText(text: String) {
    onView(withId(this)).perform(ViewActions.typeText(text))
}

fun @receiver:StringRes Int.isTextDisplayed() {
    onView(withText(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun String.isTextDisplayed() {
    onView(withText(this)).check(matches(isDisplayed()))
}

fun String.isPopupTextDisplayed() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(matches(isDisplayed()))
}

fun @receiver:StringRes Int.isPopupTextDisplayed() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(matches(ViewMatchers.isDisplayed()))
}

fun @receiver:IdRes Int.click() {
    onView(withId(this)).perform(ViewActions.click())
}

fun @receiver:StringRes Int.clickWithText() {
    onView(withText(this)).perform(ViewActions.click())
}

fun String.clickWithText() {
    onView(withText(this)).perform(ViewActions.click())
}

fun String.clickWithTextInPopup() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun @receiver:IdRes Int.swipeDown() {
    onView(withId(this)).perform(ViewActions.swipeDown())
}

fun waitFor(millis: Long) {
    onView(isRoot()).perform(waitForAction(millis))
}

private fun waitForAction(millis: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun getDescription(): String {
            return "Wait for $millis milliseconds."
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }
}