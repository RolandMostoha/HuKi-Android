package hu.mostoha.mobile.android.huki.model.network.photon

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotonQueryResponse(
    @Json(name = "features")
    val features: List<FeaturesItem>,

    @Json(name = "type")
    val type: String
)
