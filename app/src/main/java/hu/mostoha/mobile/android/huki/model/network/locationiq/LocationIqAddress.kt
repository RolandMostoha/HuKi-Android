package hu.mostoha.mobile.android.huki.model.network.locationiq

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationIqAddress(
    @Json(name = "name")
    val name: String? = null,

    @Json(name = "house_number")
    val houseNumber: String? = null,

    @Json(name = "road")
    val road: String? = null,

    @Json(name = "city")
    val city: String? = null,

    @Json(name = "county")
    val county: String? = null,

    @Json(name = "state")
    val state: String? = null,

    @Json(name = "postcode")
    val postcode: String? = null,

    @Json(name = "country")
    val country: String? = null,

    @Json(name = "country_code")
    val countryCode: String? = null,
)
