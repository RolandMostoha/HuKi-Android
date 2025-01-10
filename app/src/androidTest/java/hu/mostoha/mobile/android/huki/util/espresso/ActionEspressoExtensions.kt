package hu.mostoha.mobile.android.huki.util.espresso

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.espresso.Espresso
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher


fun clickXY(x: Int, y: Int, tapType: Tap): ViewAction {
    return GeneralClickAction(
        tapType,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val screenX = (screenPos[0] + x).toFloat()
            val screenY = (screenPos[1] + y).toFloat()

            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        0,
        0
    )
}

fun clickCenter(tapType: Tap): ViewAction {
    return GeneralClickAction(
        tapType,
        { view ->
            val screenPos = IntArray(2)
            view.getLocationOnScreen(screenPos)

            val screenX = view.width.toFloat() / 2
            val screenY = view.height.toFloat() / 2

            floatArrayOf(screenX, screenY)
        },
        Press.FINGER,
        0,
        0
    )
}

fun selectTabAtPosition(tabIndex: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "with tab at index $tabIndex"

        override fun getConstraints() = allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))

        override fun perform(uiController: UiController, view: View) {
            val tabLayout = view as TabLayout
            val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabIndex)
                ?: throw PerformException.Builder()
                    .withCause(Throwable("No tab at index $tabIndex"))
                    .build()

            tabAtIndex.select()
        }
    }
}

fun setBottomSheetStateAction(viewId: Int, state: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(CoordinatorLayout::class.java)
        }

        override fun getDescription(): String {
            return "Set BottomSheetBehavior state to $state"
        }

        override fun perform(uiController: UiController, view: View) {
            val bottomSheet = view.findViewById<View>(viewId)
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = state
        }
    }
}

fun waitFor(millis: Long) {
    Espresso.onView(ViewMatchers.isRoot()).perform(waitForAction(millis))
}

private fun waitForAction(millis: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun getDescription(): String {
            return "Wait for $millis milliseconds."
        }

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }
}

fun waitForInputFocusGain() {
    waitFor(300)
}

fun waitForBottomSheetState() {
    waitFor(500)
}

fun waitForScroll() {
    waitFor(300)
}
