package hu.mostoha.mobile.android.turistautak.util.espresso

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.not

fun @receiver:IdRes Int.isDisplayed() {
    onView(withId(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun @receiver:IdRes Int.isNotDisplayed() {
    onView(withId(this)).check(matches(not(ViewMatchers.isDisplayed())))
}

fun @receiver:IdRes Int.typeText(text: String) {
    onView(withId(this)).perform(ViewActions.typeText(text))
}

fun @receiver:StringRes Int.isTextDisplayed() {
    onView(withText(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun String.isTextDisplayed() {
    onView(withText(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun String.isPopupTextDisplayed() {
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