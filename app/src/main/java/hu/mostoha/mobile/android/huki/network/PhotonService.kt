package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.PhotonQueryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonService {

    /**
     *  Ex. https://photon.komoot.io/api?q="Mecsek"&limit=10
     */
    @GET("/api")
    suspend fun query(@Query("q") query: String, @Query("limit") limit: Int): PhotonQueryResponse

}
