package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassService {

    @GET("/api/interpreter")
    suspend fun interpreter(@Query(value = "data", encoded = true) data: String): OverpassQueryResponse

}
