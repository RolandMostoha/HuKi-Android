package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.Place
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.service.GoogleMapsUrlParser
import hu.mostoha.mobile.android.huki.service.GoogleMapsUrlParser.isGoogleMapsShortUrl
import hu.mostoha.mobile.android.huki.service.ShortUrlResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GoogleGeocodingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shortUrlResolver: ShortUrlResolver,
) {

    companion object {
        private const val EXTRA_FALLBACK_URL = "browser_fallback_url"
    }

    private val geocoder: Geocoder = Geocoder(context)

    suspend fun getPlaceByUrl(url: String): Place? {
        Timber.d("GoogleGeocode: resolving url:$url")

        val longUrl = if (url.isGoogleMapsShortUrl()) {
            val shortUrl = shortUrlResolver
                .extractUrls(url)
                .firstOrNull { it.isGoogleMapsShortUrl() }
                ?: return null

            val resolvedUri = shortUrlResolver.resolveShortUrl(shortUrl) ?: return null

            if (resolvedUri.contains("intent://")) {
                val intent = Intent.parseUri(resolvedUri, Intent.URI_INTENT_SCHEME)

                intent.getStringExtra(EXTRA_FALLBACK_URL) ?: return null
            } else {
                resolvedUri
            }
        } else {
            url
        }

        val placeName = GoogleMapsUrlParser.extractPlaceName(longUrl) ?: return null

        return geocodePlace(placeName)
    }

    private suspend fun geocodePlace(placeName: String): Place? {
        return withContext(Dispatchers.IO) {
            val address = geocodeByName(placeName).firstOrNull() ?: return@withContext null

            Timber.d("GoogleGeocode: address found for $placeName, address=$address")

            Place(
                osmId = generateOsmId(address),
                name = placeName.toMessage(),
                fullAddress = placeName,
                placeType = PlaceType.NODE,
                location = Location(address.latitude, address.longitude),
                placeFeature = PlaceFeature.GOOGLE_MAPS_SEARCH,
            )
        }
    }

    private fun geocodeByName(placeName: String): List<Address> {
        return geocoder.getFromLocationName(placeName, 1) ?: emptyList()
    }

    private fun generateOsmId(address: Address): String {
        val key = listOf(
            address.latitude,
            address.longitude,
            address.featureName ?: "",
            address.getAddressLine(0) ?: ""
        ).joinToString("|")

        return "GM-${key.hashCode()}"
    }

}
