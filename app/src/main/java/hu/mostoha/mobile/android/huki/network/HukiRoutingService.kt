package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteRequest
import hu.mostoha.mobile.android.huki.model.network.graphhopper.RouteResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Self-hosted GraphHopper covering Hungary only. The key must stay in a header, the server logs request URLs.
 */
interface HukiRoutingService {

    @POST("route")
    suspend fun getRoute(
        @Body routeRequest: RouteRequest,
        @Header(HEADER_API_KEY) key: String = BuildConfig.HUKI_ROUTING_API_KEY
    ): RouteResponse

    companion object {
        const val HEADER_API_KEY = "X-HuKi-Key"
    }

}
