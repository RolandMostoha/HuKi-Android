package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.model.network.locationiq.LocationIqPlace
import hu.mostoha.mobile.android.huki.network.interceptor.TimeoutInterceptor.Companion.HEADER_TIMEOUT
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface LocationIqService {

    companion object {
        private const val REVERSE_GEOCODER_TIMEOUT = 3000

        const val LOCATION_IQ_BB_NORTH_INDEX = 1
        const val LOCATION_IQ_BB_EAST_INDEX = 3
        const val LOCATION_IQ_BB_SOUTH_INDEX = 0
        const val LOCATION_IQ_BB_WEST_INDEX = 2
    }

    @GET("autocomplete")
    suspend fun autocomplete(
        @Query("q") query: String,
        @Query("viewbox") viewBox: String,
        @Query("importancesort") importanceSort: Int = 0,
        @Query("normalizecity") normalizeCity: Int = 1,
        @Query("countrycodes") countryCodes: String = "hu",
        @Query("accept-language") acceptLanguage: String = "hu",
        @Query("limit") limit: Int = 20,
        @Query("key") key: String = BuildConfig.LOCATION_IQ_API_KEY,
    ): List<LocationIqPlace>

    @Headers("$HEADER_TIMEOUT:$REVERSE_GEOCODER_TIMEOUT")
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("zoom") zoom: Int = 18,
        @Query("extratags") extraTags: Int = 0,
        @Query("normalizeaddress") normalizeAddress: Int = 1,
        @Query("format") format: String = "json",
        @Query("accept-language") acceptLanguage: String = "hu",
        @Query("key") key: String = BuildConfig.LOCATION_IQ_API_KEY,
    ): LocationIqPlace

}
