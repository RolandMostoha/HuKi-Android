package hu.mostoha.mobile.android.huki.service

import android.net.Uri
import timber.log.Timber
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object GoogleMapsUrlParser {

    // Regex to capture the encoded place name from a Google Maps URL path.
    private val placeNameRegex = """/place/([^/]+)""".toRegex()

    fun extractPlaceName(url: String): String? {
        val matchResult = placeNameRegex.find(url)

        if (matchResult != null) {
            try {
                val encodedName = matchResult.groupValues[1]

                // Decode the captured group (e.g., "D%C3%B6m%C3%B6s...") into "Dömös..."
                val decoded = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.name())
                Timber.d("GoogleMapsUrlParser: decoded place name: $decoded")

                return decoded
            } catch (e: Exception) {
                Timber.w(e, "GoogleMapsParser: failed to decode URL-encoded place name.")
                return null
            }
        } else {
            Timber.w("GoogleMapsUrlParser: place name was not extracted, using raw text.")
            return null
        }
    }

    fun String.isGoogleMapsUrl() = this.isGoogleMapsShortUrl() || this.isGoogleMapsLongUrl()

    fun String.isGoogleMapsShortUrl(): Boolean {
        val hosts = listOf("maps.app.goo.gl", "goo.gl")
        val url = this

        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return false

            return hosts.contains(host)
        } catch (exception: Exception) {
            Timber.e(exception, "GoogleMapsUrlParser: not valid URI: $url")
            false
        }
    }

    fun String.isGoogleMapsLongUrl(): Boolean {
        val hosts = listOf("www.google.com", "google.com", "maps.google.com")
        val url = this

        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return false

            return hosts.contains(host)
        } catch (exception: Exception) {
            Timber.e(exception, "GoogleMapsUrlParser: not valid URI: $url")
            false
        }
    }

}
