package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.model.network.graphhopper.GeocodingResponse
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteRequest
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GraphhopperService {

    @POST("route")
    suspend fun getRoute(
        @Body routeRequest: RouteRequest,
        @Query("key") key: String = BuildConfig.GRAPHHOPPER_API_KEY
    ): RouteResponse

    @GET("geocode")
    suspend fun reverseGeocode(
        @Query("reverse") boolean: Boolean = true,
        @Query("point") point: String,
        @Query("key") key: String = BuildConfig.GRAPHHOPPER_API_KEY
    ): GeocodingResponse

}
