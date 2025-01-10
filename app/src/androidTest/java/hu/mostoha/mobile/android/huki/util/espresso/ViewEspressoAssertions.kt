package hu.mostoha.mobile.android.huki.util.espresso

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.util.testAppContext
import hu.mostoha.mobile.android.huki.util.testContext
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun @receiver:IdRes Int.isDisplayed() {
    onView(withId(this)).check(matches(ViewMatchers.isDisplayed()))
}

fun @receiver:IdRes Int.isCompletelyDisplayed() {
    onView(withId(this)).check(matches(ViewMatchers.isCompletelyDisplayed()))
}

fun @receiver:IdRes Int.isNotDisplayed() {
    onView(withId(this)).check(matches(not(ViewMatchers.isDisplayed())))
}

fun @receiver:IdRes Int.isNotCompletelyDisplayed() {
    onView(withId(this)).check(matches(not(isCompletelyDisplayed())))
}

fun @receiver:IdRes Int.doesNotExist() {
    onView(withId(this)).check(ViewAssertions.doesNotExist())
}

fun @receiver:IdRes Int.isDisplayedWithText(text: String) {
    onView(withId(this)).check(matches(withText(text)))
}

fun @receiver:IdRes Int.isDisplayedContainsText(text: String) {
    onView(withId(this)).check(matches(withText(containsString(text))))
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

fun Message.isTextDisplayed() {
    this.resolve(testContext).isTextDisplayed()
}

fun Regex.isRegexTextDisplayed() {
    onView(withPattern(this)).check(matches(isDisplayed()))
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

fun Message.isPopupTextDisplayed() {
    this.resolve(testContext).isPopupTextDisplayed()
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

fun String.isPopupTextNotDisplayed() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(matches(not(isDisplayed())))
}

fun String.isPopupTextNotExists() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .check(ViewAssertions.doesNotExist())
}

fun @receiver:StringRes Int.hasFocus() {
    onView(withId(this)).check(matches(ViewMatchers.hasFocus()))
}

fun @receiver:StringRes Int.hasNoFocus() {
    onView(withId(this)).check(matches(not(ViewMatchers.hasFocus())))
}

fun @receiver:StringRes Int.isSnackbarMessageDisplayed() {
    onView(withId(com.google.android.material.R.id.snackbar_text))
        .check(matches(withText(this)))
}

fun String.isSnackbarMessageDisplayed() {
    onView(withId(com.google.android.material.R.id.snackbar_text))
        .check(matches(withText(this)))
}

fun @receiver:IdRes Int.click() {
    onView(withId(this))
        .perform(ViewActions.click())
}

fun @receiver:IdRes Int.clickWithScroll() {
    onView(withId(this))
        .perform(scrollTo())
        .perform(ViewActions.click())
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

fun @receiver:IdRes Int.clickWithSibling(text: String) {
    onView(
        allOf(
            withId(this),
            hasSibling(withText(text))
        )
    ).perform(ViewActions.click())
}

fun @receiver:IdRes Int.clickInPopup() {
    onView(withId(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun @receiver:StringRes Int.clickWithText() {
    onView(withText(this))
        .perform(ViewActions.click())
}

fun Message.clickWithText() {
    this.resolve(testAppContext).clickWithText()
}

fun @receiver:StringRes Int.clickWithTextWithScroll() {
    onView(withText(this))
        .perform(scrollTo())
        .perform(ViewActions.click())
}

fun Message.clickWithTextWithScroll() {
    onView(withText(this.resolve(testAppContext)))
        .perform(scrollTo())
        .perform(ViewActions.click())
}

fun @receiver:StringRes Int.clickWithTextInPopup() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun Message.clickWithTextInPopup() {
    this.resolve(testContext).clickWithTextInPopup()
}

fun String.clickWithText() {
    onView(withText(this)).perform(ViewActions.click())
}

fun String.clickWithTextInPopup() {
    onView(withText(this))
        .inRoot(RootMatchers.isPlatformPopup())
        .perform(ViewActions.click())
}

fun @receiver:IdRes Int.clickImeActionButton() {
    onView(withId(this)).perform(pressImeActionButton())
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

fun String.clickWithContentDescription() {
    onView(withContentDescription(this)).perform(ViewActions.click())
}

fun @receiver:IdRes Int.swipeDown() {
    onView(withId(this)).perform(ViewActions.swipeDown())
    waitForScroll()
}

fun @receiver:IdRes Int.swipeUp() {
    onView(withId(this)).perform(ViewActions.swipeUp())
    waitForScroll()
}

fun @receiver:IdRes Int.swipeLeft() {
    onView(withId(this)).perform(ViewActions.swipeLeft())
    waitForScroll()
}

fun @receiver:IdRes Int.swipeRight() {
    onView(withId(this)).perform(ViewActions.swipeRight())
    waitForScroll()
}


fun @receiver:IdRes Int.selectTab(tabIndex: Int) {
    onView(withId(this)).perform(selectTabAtPosition(tabIndex))
}

fun @receiver:IdRes Int.hasDisplayedItemAtPosition(position: Int) {
    onView(withId(this)).check(matches(hasItemAtPosition(position, ViewMatchers.isDisplayed())))
}

fun @receiver:IdRes Int.setBottomSheetState(@IdRes containerViewId: Int, state: Int) {
    onView(withId(this)).perform(setBottomSheetStateAction(containerViewId, state))
}

fun hasItemAtPosition(position: Int, matcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("has item at position $position : ")
            matcher.describeTo(description)
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) ?: return false

            return matcher.matches(viewHolder.itemView)
        }
    }
}

fun @receiver:IdRes Int.hasDescendantWithText(position: Int, text: String) {
    onView(withItemAtPosition(withId(this), position))
        .check(matches(ViewMatchers.hasDescendant(withText(text))))
}

fun @receiver:IdRes Int.hasDescendantWithText(position: Int, text: Message) {
    hasDescendantWithText(position, text.resolve(testContext))
}


fun isKeyboardShown(): Boolean {
    val inputMethodManager = InstrumentationRegistry
        .getInstrumentation()
        .targetContext
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    return inputMethodManager.isAcceptingText
}

private fun withItemAtPosition(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        private var currentIndex = 0

        override fun describeTo(description: Description?) {
            description?.appendText("with index: ")
            description?.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View?): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}

private fun withPattern(regex: Regex): Matcher<View> {
    return object : BoundedMatcher<View, TextView>(TextView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with text pattern: ")
            description.appendText(regex.toString())
        }

        override fun matchesSafely(textView: TextView): Boolean {
            val text = textView.text
            return regex.matches(text)
        }
    }
}
