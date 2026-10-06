package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.BuildConfig

object NetworkConfig {

    const val BASE_URL_OVERPASS = "https://overpass-api.de"
    const val BASE_URL_PHOTON = "https://photon.komoot.io"
    const val BASE_URL_LOCATION_IQ = "https://eu1.locationiq.com/v1/"
    const val BASE_URL_GRAPHHOPPER_API = "https://graphhopper.com/api/1/"
    const val BASE_URL_HUKI_ROUTING = "https://huki-routing-550684341915.europe-west1.run.app/"

    const val DEFAULT_TIMEOUT_S = 15
    const val DEFAULT_TIMEOUT_MS = DEFAULT_TIMEOUT_S * 1000

    const val HUKI_USER_AGENT = "HuKi/${BuildConfig.VERSION_NAME} (Android; ${BuildConfig.APPLICATION_ID})"

}
