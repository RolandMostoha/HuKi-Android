package hu.mostoha.mobile.android.turistautak.extensions

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat


fun TextView.setDrawableStart(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}