package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.network.interceptor.TimeoutInterceptor.Companion.HEADER_TIMEOUT
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface OverpassService {

    companion object {
        const val OVERPASS_TIMEOUT_MS = 30000
    }

    @Headers("$HEADER_TIMEOUT:$OVERPASS_TIMEOUT_MS")
    @GET("/api/interpreter")
    suspend fun interpreter(@Query(value = "data", encoded = true) data: String): OverpassQueryResponse

}
