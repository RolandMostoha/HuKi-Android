package hu.mostoha.mobile.android.turistautak.extensions

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