package hu.mostoha.mobile.android.huki.model.network.overpass

import com.squareup.moshi.Json

enum class ElementType {
    @Json(name = "relation")
    RELATION,

    @Json(name = "way")
    WAY,

    @Json(name = "node")
    NODE
}
