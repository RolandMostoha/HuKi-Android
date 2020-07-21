package hu.mostoha.mobile.android.turistautak.network

import com.squareup.moshi.Json

data class OverpassQueryResult(
    @field:Json(name = "elements")
    var elements: List<Element>
)

data class Element(
    @field:Json(name = "type")
    var type: String,

    @field:Json(name = "id")
    var id: Long,

    @field:Json(name = "lat")
    var lat: Double?,

    @field:Json(name = "lon")
    var lon: Double?,

    @field:Json(name = "tags")
    var tags: Tags?
)

data class Tags(
    @field:Json(name = "type")
    var type: String? = null,

    @field:Json(name = "amenity")
    var amenity: String? = null,

    @field:Json(name = "name")
    var name: String? = null,

    @field:Json(name = "phone")
    var phone: String? = null,

    @field:Json(name = "contact:email")
    var contactEmail: String? = null,

    @field:Json(name = "website")
    var website: String? = null,

    @field:Json(name = "addr:city")
    var addressCity: String? = null,

    @field:Json(name = "addr:postcode")
    var addressPostCode: String? = null,

    @field:Json(name = "addr:street")
    var addressStreet: String? = null,

    @field:Json(name = "addr:housenumber")
    var addressHouseNumber: String? = null,

    @field:Json(name = "wheelchair")
    var wheelchair: String? = null,

    @field:Json(name = "wheelchair:description")
    var wheelchairDescription: String? = null,

    @field:Json(name = "opening_hours")
    var openingHours: String? = null,

    @field:Json(name = "internet_access")
    var internetAccess: String? = null,

    @field:Json(name = "fee")
    var fee: String? = null,

    @field:Json(name = "operator")
    var operator: String? = null
)