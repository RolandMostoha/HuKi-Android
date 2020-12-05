package hu.mostoha.mobile.android.turistautak.extensions

import android.graphics.drawable.Animatable
import android.widget.ImageView

fun ImageView.startDrawableAnimation() {
    val drawable = this.drawable
    if (drawable is Animatable) {
        (drawable as Animatable).start()
    }
}