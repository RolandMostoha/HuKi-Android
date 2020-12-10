package hu.mostoha.mobile.android.turistautak.util

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.util.GeoPoint

fun AppCompatActivity.startGoogleDirections(geoPoint: GeoPoint) {
    val latLng = "${geoPoint.latitude},${geoPoint.longitude}"
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latLng")
    val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    startActivity(mapIntent)
}
