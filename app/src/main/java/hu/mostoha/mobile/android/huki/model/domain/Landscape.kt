package hu.mostoha.mobile.android.huki.model.domain

data class Landscape(
    val osmId: String,
    val name: String,
    val type: LandscapeType,
    val center: Location
)
