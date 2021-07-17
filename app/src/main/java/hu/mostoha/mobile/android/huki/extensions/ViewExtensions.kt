package hu.mostoha.mobile.android.huki.extensions

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

fun View.gone() {
    this.isGone = true
}

fun View.visible() {
    this.isVisible = true
}

fun View.invisible() {
    this.isInvisible = true
}

fun View.visibleOrGone(visible: Boolean) {
    if (visible) visible() else gone()
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun List<View>.showOnly(vararg views: View) {
    forEach {
        it.gone()
    }
    views.forEach { targetView ->
        if (!this.contains(targetView)) error("Exclusive view list not contains the target view!")

        targetView.visible()
    }
}
