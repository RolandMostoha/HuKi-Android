package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.isVisible

/**
 * Keeps the view above the bottom sheets marked with `layout_insetEdge="bottom"`
 */
class BottomSheetDodgeBehavior<V : View>(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<V>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency.isInsetEdgeBottom()
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)
        child.translationY = parent.dodgeTranslationY(child)

        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        val translationY = parent.dodgeTranslationY(child)
        if (child.translationY == translationY) {
            return false
        }

        child.translationY = translationY

        return true
    }

    private fun CoordinatorLayout.dodgeTranslationY(child: V): Float {
        val insetTop = children
            .filter { it !== child && it.isVisible && it.isInsetEdgeBottom() }
            .minOfOrNull { it.top + it.translationY }
            ?: return 0f

        return minOf(0f, insetTop - (child.bottom - child.paddingBottom))
    }

    private fun View.isInsetEdgeBottom(): Boolean {
        val coordinatorLayoutParams = layoutParams as? CoordinatorLayout.LayoutParams ?: return false

        return coordinatorLayoutParams.insetEdge and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM
    }

}
