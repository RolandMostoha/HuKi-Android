package hu.mostoha.mobile.android.huki.extensions

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import hu.mostoha.mobile.android.huki.R

fun AppCompatActivity.setStatusBarColor(@ColorRes colorRes: Int) {
    window.apply {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = ContextCompat.getColor(this@setStatusBarColor, colorRes)
    }
}

fun View.applyTopMarginForStatusBar(appCompatActivity: AppCompatActivity) {
    val viewTopMargin = marginTop

    ViewCompat.setOnApplyWindowInsetsListener(appCompatActivity.findViewById(R.id.homeContainer)) { _, insets ->
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(top = viewTopMargin + insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
        }
        WindowInsetsCompat.CONSUMED
    }
}