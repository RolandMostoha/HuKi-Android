package hu.mostoha.mobile.android.huki.extensions

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat


fun TextView.setDrawableStart(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

fun TextView.setDrawableTop(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
}

fun TextView.setTextOrGone(text: String?) {
    if (text == null) {
        gone()
    } else {
        visible()
        this.text = text
    }
}
