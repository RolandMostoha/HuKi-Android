package hu.mostoha.mobile.android.huki.util.espresso

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap


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
