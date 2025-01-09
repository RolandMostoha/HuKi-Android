package hu.mostoha.mobile.android.huki.model.network.overpass

import com.google.gson.annotations.JsonAdapter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import hu.mostoha.mobile.android.huki.network.adapter.JsonMapAdapter

@JsonClass(generateAdapter = true)
data class Element(
    @Json(name = "type")
    val type: ElementType,

    @Json(name = "id")
    val id: Long,

    @Json(name = "tags")
    @JsonAdapter(JsonMapAdapter::class)
    val tags: Map<String, String>? = null,

    @Json(name = "lat")
    val lat: Double? = null,

    @Json(name = "lon")
    val lon: Double? = null,

    @Json(name = "geometry")
    val geometry: List<Geom>? = null,

    @Json(name = "members")
    val members: List<Member>? = null,

    @Json(name = "bounds")
    val bounds: Bounds? = null
)
