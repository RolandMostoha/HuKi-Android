package hu.mostoha.mobile.android.huki.model.domain

data class PlaceAddress(
    val name: String,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    val fullAddress: String? = null,
)
