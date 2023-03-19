package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SnappedWaypoints(

    @Json(name = "coordinates")
    val coordinates: List<List<Double>>,

    @Json(name = "type")
    val type: String

)
