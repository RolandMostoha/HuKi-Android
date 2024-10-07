package hu.mostoha.mobile.android.huki.deeplink

import android.content.Intent
import hu.mostoha.mobile.android.huki.model.domain.DeeplinkEvent
import timber.log.Timber
import javax.inject.Inject

class DeeplinkHandler @Inject constructor() {

    companion object {
        private const val HUKI_HOST = "huki.hu"

        private const val PATH_LANDSCAPE = "landscape"
        private const val PATH_PLACE = "place"

        private const val QUERY_OSM_ID = "osmId"
        private const val QUERY_LAT = "lat"
        private const val QUERY_LON = "lon"
    }

    fun handleDeeplink(intent: Intent): DeeplinkEvent? {
        val action = intent.action
        val data = intent.data

        if (action == Intent.ACTION_VIEW && data != null && data.host == HUKI_HOST) {
            Timber.d("Deeplink: opened $data")

            val lastPathSegment = data.lastPathSegment

            when (lastPathSegment) {
                PATH_LANDSCAPE -> {
                    val osmId = data.getQueryParameter(QUERY_OSM_ID)

                    if (osmId != null) {
                        return DeeplinkEvent.LandscapeDetails(osmId)
                    }
                }
                PATH_PLACE -> {
                    val lat = data.getQueryParameter(QUERY_LAT)?.toDoubleOrNull()
                    val lon = data.getQueryParameter(QUERY_LON)?.toDoubleOrNull()

                    if (lat != null && lon != null) {
                        return DeeplinkEvent.PlaceDetails(lat, lon)
                    }
                }
                else -> {
                    Timber.d("Deeplink: unknown deeplink $data")
                }
            }
        }

        return null
    }

}
