package hu.mostoha.mobile.android.huki.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

fun Context.isLocationPermissionGranted(): Boolean {
    return isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) || isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
}

fun Map<String, Boolean>.isLocationPermissionGranted(): Boolean {
    val isFineLocationEnabled = this.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
    val isCoarseLocationEnabled = this.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

    return isFineLocationEnabled || isCoarseLocationEnabled
}

private fun Context.isGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.shouldShowLocationRationale(): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
}
