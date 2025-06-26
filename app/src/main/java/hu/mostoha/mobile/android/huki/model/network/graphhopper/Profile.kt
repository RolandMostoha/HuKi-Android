package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json

enum class Profile {

    @Json(name = "hike")
    HIKE,

    @Json(name = "foot")
    FOOT,

    @Json(name = "bike")
    BIKE,

    @Json(name = "mtb")
    MTB,

}
