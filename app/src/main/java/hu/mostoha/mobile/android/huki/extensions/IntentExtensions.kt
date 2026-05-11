package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.content.Intent
import android.net.MailTo
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.deeplink.DeeplinkHandler.Companion.HUKI_HOST
import hu.mostoha.mobile.android.huki.service.GoogleMapsUrlParser.isGoogleMapsUrl
import hu.mostoha.mobile.android.huki.util.GOOGLE_MAPS_DIRECTIONS_URL
import org.osmdroid.util.GeoPoint
import timber.log.Timber

fun Context.startGoogleMapsDirectionsIntent(geoPoint: GeoPoint) {
    val mapsUrl = GOOGLE_MAPS_DIRECTIONS_URL.format(geoPoint.latitude, geoPoint.longitude)

    Timber.d("Google Maps direction intent started with URL: $mapsUrl")

    startActivity(Intent(Intent.ACTION_VIEW, mapsUrl.toUri()))
}

fun Context.openUrl(url: String) {
    Timber.d("URL Intent started with URL: $url")

    val uri = url.toUri()

    val intent = Intent(Intent.ACTION_VIEW, uri)

    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Timber.e("No resolved activity found to handle URL: $url, trying to open anyways...")

        try {
            startActivity(intent)
        } catch (exception: Exception) {
            Timber.e(exception, "Couldn't open URL: $url")
        }
    }
}

fun Context.startEmailIntent(email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = MailTo.MAILTO_SCHEME.toUri()
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

    if (scheme.isNullOrEmpty() || uri == null) {
        return false
    }

    val isTypeGpx = type?.contains("gpx") ?: false
    val isOctetStream = type?.contains("application/octet-stream") ?: false
    val isSchemeGpx = scheme.contains("gpx")
    val isUriPathGpx = uri.path?.contains("gpx") ?: false

    return isTypeGpx || isOctetStream || isSchemeGpx || isUriPathGpx
}

fun Intent.isTextIntent(): Boolean {
    return action == Intent.ACTION_SEND && type == "text/plain"
}

fun Intent.isGoogleMapsIntent(): Boolean {
    val hasTextExtra = hasExtra(Intent.EXTRA_TEXT)
    val text = getStringExtra(Intent.EXTRA_TEXT)
    return isTextIntent() && hasTextExtra && text!!.isGoogleMapsUrl()
}

fun Intent.isDeeplink(): Boolean {
    return action == Intent.ACTION_VIEW && data?.host == HUKI_HOST
}

fun Context.shareGpxFile(uri: Uri) {
    val providerUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", uri.toFile())

    // Base ACTION_SEND Intent to share social/email apps
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, providerUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Extra ACTION_VIEW GPX Intent for map apps (Garmin Connect, OsmAnd, Locus)
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(providerUri, "application/gpx+xml")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val viewTargets = packageManager.queryIntentActivities(viewIntent, 0)
        .map { resolveInfo ->
            Intent(viewIntent).setPackage(resolveInfo.activityInfo.packageName)
        }

    val chooser = Intent.createChooser(sendIntent, null).apply {
        if (viewTargets.isNotEmpty()) {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, viewTargets.toTypedArray())
        }
    }

    startActivity(chooser)
}
