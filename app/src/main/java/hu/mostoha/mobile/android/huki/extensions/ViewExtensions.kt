package hu.mostoha.mobile.android.huki.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog

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

fun FloatingActionButton.showOrHide(visible: Boolean) {
    if (visible) show() else hide()
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun List<View>.showOnly(vararg views: View) {
    forEach {
        it.gone()
    }
    views.forEach { targetView ->
        if (!this.contains(targetView)) {
            error("Exclusive view list does not contain the target view!")
        }

        targetView.visible()
    }
}

fun List<BottomSheetDialog>.showOnly(vararg dialogs: BottomSheetDialog) {
    forEach { dialog ->
        dialog.hide()
    }
    dialogs.forEach { targetDialog ->
        if (!this.contains(targetDialog)) {
            error("Exclusive dialog list does not contain the target dialog!")
        }

        targetDialog.show()
    }
}

fun List<BottomSheetDialog>.hideAll() {
    forEach { dialog ->
        dialog.hide()
    }
}

fun View.clearBackground() {
    backgroundTintMode = PorterDuff.Mode.CLEAR
    backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
    setBackgroundColor(Color.TRANSPARENT)
}
