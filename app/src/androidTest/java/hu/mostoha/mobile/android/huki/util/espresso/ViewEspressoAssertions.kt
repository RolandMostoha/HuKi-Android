package hu.mostoha.mobile.android.huki.util.espresso

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
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

fun @receiver:StringRes Int.isTextNotDisplayed() {
    onView(withText(this)).check(matches(not(ViewMatchers.isDisplayed())))
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

fun @receiver:StringRes Int.isPopupTextNotDisplayed() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(matches(not(ViewMatchers.isDisplayed())))
}

fun @receiver:StringRes Int.isPopupTextNotExists() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(ViewAssertions.doesNotExist())
}

fun @receiver:IdRes Int.click() {
    onView(withId(this)).perform(ViewActions.click())
}

fun @receiver:IdRes Int.longClick() {
    onView(withId(this)).perform(ViewActions.longClick())
}

fun @receiver:IdRes Int.longClickCenter() {
    onView(withId(this)).perform(clickCenter(Tap.LONG))
}

fun @receiver:IdRes Int.clickWithSibling(@StringRes stringRes: Int) {
    onView(
        allOf(
            withId(this),
            hasSibling(withText(stringRes))
        )
    ).perform(ViewActions.click())
}

fun @receiver:IdRes Int.clickInPopup() {
    onView(withId(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun @receiver:StringRes Int.clickWithText() {
    onView(withText(this)).perform(ViewActions.click())
}

fun @receiver:StringRes Int.clickWithTextInPopup() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun String.clickWithText() {
    onView(withText(this)).perform(ViewActions.click())
}

fun String.clickWithTextInPopup() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun @receiver:StringRes Int.isDisplayedWithContentDescription() {
    onView(withContentDescription(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun @receiver:StringRes Int.isNotDisplayedWithContentDescription() {
    onView(withContentDescription(this)).check(matches(not(ViewMatchers.isDisplayed())))
}

fun @receiver:StringRes Int.doesNotExistWithContentDescription() {
    onView(withContentDescription(this)).check(ViewAssertions.doesNotExist())
}

fun @receiver:StringRes Int.clickWithContentDescription() {
    onView(withContentDescription(this)).perform(ViewActions.click())
}

fun @receiver:IdRes Int.swipeDown() {
    onView(withId(this)).perform(ViewActions.swipeDown())
}

fun @receiver:IdRes Int.selectTab(tabIndex: Int) {
    onView(withId(this)).perform(selectTabAtPosition(tabIndex))
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
