package hu.mostoha.mobile.android.huki.util.espresso

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers.allOf


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
