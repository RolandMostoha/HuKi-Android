package hu.mostoha.mobile.android.turistautak.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

fun Context.isLocationPermissionsGranted(): Boolean {
    return isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
}

private fun Context.isGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.checkLocationPermissions(
    onPermissionsChecked: () -> Unit,
    onPermissionRationaleShouldBeShown: ((PermissionToken) -> Unit)? = null
) {
    checkPermissions(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
        onPermissionsChecked,
        onPermissionRationaleShouldBeShown
    )
}

fun Context.checkPermissions(
    permissions: List<String>,
    onPermissionsChecked: () -> Unit,
    onPermissionRationaleShouldBeShown: ((PermissionToken) -> Unit)? = null
) {
    Dexter.withContext(this)
        .withPermissions(permissions)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(permissionsReport: MultiplePermissionsReport) {
                onPermissionsChecked.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                request: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                onPermissionRationaleShouldBeShown?.invoke(token)
            }
        })
        .check()
}