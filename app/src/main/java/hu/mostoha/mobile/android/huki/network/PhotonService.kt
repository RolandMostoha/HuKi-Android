package hu.mostoha.mobile.android.huki.network

import hu.mostoha.mobile.android.huki.model.network.PhotonQueryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonService {
    /**
     *  Ex. https://photon.komoot.de//api?lat=47.1625&lon=19.5033&bbox=16.20229,45.75948,22.71053,48.62385&q="Mecsek"&limit=10
     */
    @GET("/api?lat=47.1625&lon=19.5033&bbox=16.20229,45.75948,22.71053,48.62385")
    suspend fun query(@Query("q") query: String, @Query("limit") limit: Int): PhotonQueryResponse
}