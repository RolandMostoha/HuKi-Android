package hu.mostoha.mobile.android.turistautak.extensions

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import hu.mostoha.mobile.android.turistautak.R
import org.osmdroid.util.GeoPoint

fun AppCompatActivity.setStatusBarColor(@ColorRes colorRes: Int) {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        statusBarColor = ContextCompat.getColor(this@setStatusBarColor, colorRes)
    }
}

fun AppCompatActivity.setFullScreenAndLightSystemBars() {
    var flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }
    window.decorView.systemUiVisibility = flags
}

fun View.applyTopMarginForStatusBar(appCompatActivity: AppCompatActivity) {
    val viewTopMargin = marginTop

    ViewCompat.setOnApplyWindowInsetsListener(appCompatActivity.findViewById(R.id.homeContainer)) { _, insets ->
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(top = viewTopMargin + insets.systemWindowInsetTop)
        }
        insets.consumeSystemWindowInsets()
    }
}

fun AppCompatActivity.startDirectionsTo(geoPoint: GeoPoint) {
    val latLng = "${geoPoint.latitude},${geoPoint.longitude}"
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latLng")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    startActivity(mapIntent)
}