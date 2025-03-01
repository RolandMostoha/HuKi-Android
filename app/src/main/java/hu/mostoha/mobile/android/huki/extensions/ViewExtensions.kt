package hu.mostoha.mobile.android.huki.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
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

fun ImageView.setImageOrGone(@DrawableRes imageResId: Int?) {
    if (imageResId != null) {
        setImageResource(imageResId)
        visible()
    } else {
        gone()
    }
}

fun FloatingActionButton.showOrHide(visible: Boolean) {
    if (visible) show() else hide()
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun List<View>.showOnly(vararg viewsToShow: View) {
    this.forEach { view ->
        if (viewsToShow.contains(view)) {
            view.visible()
        } else {
            view.gone()
        }
    }
}

fun List<BottomSheetDialog>.showOnly(vararg dialogsToShow: BottomSheetDialog) {
    this.forEach { dialog ->
        if (dialogsToShow.contains(dialog)) {
            dialog.show()
        } else {
            dialog.hide()
        }
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
