package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OverpassQueryResponse(
    @Json(name = "elements")
    var elements: List<Element>
)
