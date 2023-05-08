package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import android.provider.Browser
import androidx.core.app.ShareCompat
import hu.mostoha.mobile.android.huki.util.GOOGLE_MAPS_DIRECTIONS_URL
import org.osmdroid.util.GeoPoint

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

fun Intent.isGpxFileIntent(): Boolean {
    val type = this.type
    val scheme = this.scheme
    val uri = this.data

    if (type.isNullOrEmpty() || scheme.isNullOrEmpty() || uri == null) {
        return false
    }

    val isTypeGpx = type.contains("gpx")
    val isSchemeGpx = scheme.contains("gpx")
    val isUriPathGpx = uri.path?.contains("gpx") ?: false
    val isOctetStream = type.contains("application/octet-stream")

    return isTypeGpx || isOctetStream || isSchemeGpx || isUriPathGpx
}

fun Context.shareFile(uri: Uri) {
    ShareCompat.IntentBuilder(this)
        .setStream(uri)
        .setType("*/*")
        .startChooser()
}
