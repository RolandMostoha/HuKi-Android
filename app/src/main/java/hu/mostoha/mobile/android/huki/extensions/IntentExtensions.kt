package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.provider.Browser
import org.osmdroid.util.GeoPoint

const val GOOGLE_MAPS_DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1&destination=%s,%s"

fun Context.startGoogleMapsDirectionsIntent(geoPoint: GeoPoint) {
    val mapsUrl = GOOGLE_MAPS_DIRECTIONS_URL.format(geoPoint.latitude, geoPoint.longitude)

    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
}

fun Context.startUrlIntent(url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

fun Context.startEmailIntent(email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse(MailTo.MAILTO_SCHEME)
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}
