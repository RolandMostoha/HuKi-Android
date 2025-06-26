package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json

enum class Algorithm {

    @Json(name = "round_trip")
    ROUND_TRIP,

    @Json(name = "alternative_route")
    ALTERNATIVE_ROUTE,

}
