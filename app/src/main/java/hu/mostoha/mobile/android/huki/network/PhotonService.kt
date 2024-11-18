package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.photon.PhotonQueryResponse
import hu.mostoha.mobile.android.huki.network.interceptor.TimeoutInterceptor.Companion.HEADER_TIMEOUT
import hu.mostoha.mobile.android.huki.util.BUDAPEST_LOCATION
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface PhotonService {

    companion object {
        const val GEOCODE_TIMEOUT = 3000
    }

    /**
     *  E.g. https://photon.komoot.io/api?q="Mecsek"&limit=10&lat=47.5452098&lon=19.1130386
     */
    @GET("/api")
    suspend fun query(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("lat") latitude: Double = BUDAPEST_LOCATION.latitude,
        @Query("lon") longitude: Double = BUDAPEST_LOCATION.longitude,
    ): PhotonQueryResponse

    /**
     *  E.g. https://photon.komoot.io/reverse?limit=10&lat=47.5452098&lon=19.1130386
     */
    @Headers("$HEADER_TIMEOUT:$GEOCODE_TIMEOUT")
    @GET("/reverse")
    suspend fun reverseGeocode(
        @Query("limit") limit: Int,
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
    ): PhotonQueryResponse

}
