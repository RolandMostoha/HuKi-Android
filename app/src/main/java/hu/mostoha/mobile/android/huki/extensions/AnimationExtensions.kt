package hu.mostoha.mobile.android.huki.extensions

import android.animation.ValueAnimator
import android.view.View

fun View.animateTopPadding(padding: Int, duration: Long = 200) {
    val animator = ValueAnimator.ofInt(0, padding)
    animator.addUpdateListener { valueAnimator ->
        this.setPadding(0, valueAnimator.animatedValue as Int, 0, 0)
    }
    animator.duration = duration
    animator.start()
}
