package hu.mostoha.mobile.android.huki.model.network.photon

import com.squareup.moshi.Json

enum class OsmType {
    @Json(name = "R")
    RELATION,

    @Json(name = "W")
    WAY,

    @Json(name = "N")
    NODE
}
