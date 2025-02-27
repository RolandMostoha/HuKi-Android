package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import hu.mostoha.mobile.android.huki.network.interceptor.TimeoutInterceptor.Companion.HEADER_TIMEOUT
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PhotonService {

    companion object {
        private const val REVERSE_GEOCODER_TIMEOUT = 3000
        private const val AUTOCOMPLETE_ITEM_LIMIT = 20
    }

    /**
     *  E.g. https://photon.komoot.io/api?q="Mecsek"&limit=10&lat=47.5452098&lon=19.1130386
     */
    @GET("/api")
    suspend fun query(
        @Query("q") query: String,
        @Query("lat") latitude: Double = BUDAPEST_LOCATION.latitude,
        @Query("lon") longitude: Double = BUDAPEST_LOCATION.longitude,
        @Query("limit") limit: Int = AUTOCOMPLETE_ITEM_LIMIT,
    ): PhotonQueryResponse

    /**
     *  E.g. https://photon.komoot.io/reverse?limit=10&lat=47.5452098&lon=19.1130386
     */
    @Headers("$HEADER_TIMEOUT:$REVERSE_GEOCODER_TIMEOUT")
    @GET("/reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
    ): PhotonQueryResponse

}
