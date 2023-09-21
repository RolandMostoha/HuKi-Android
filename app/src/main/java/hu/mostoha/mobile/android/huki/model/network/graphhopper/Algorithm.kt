package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json

enum class Algorithm {

    @field:Json(name = "round_trip")
    ROUND_TRIP,

    @field:Json(name = "alternative_route")
    ALTERNATIVE_ROUTE,

}
