package hu.mostoha.mobile.android.huki.extensions

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Activity.setStatusBarColor(@ColorRes colorRes: Int) {
    window.apply {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = ContextCompat.getColor(this@setStatusBarColor, colorRes)
    }
}
