package hu.mostoha.mobile.android.huki.model.domain

data class OktStampWaypoint(
    val title: String,
    val description: String,
    val location: Location,
    val stampTag: OktStampTag,
)
