package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.StringRes

data class Destination(
    val osmId: String,
    val relId: String? = null,
    val name: String,
    val town: String,
    val type: DestinationType,
    val location: Location,
    @StringRes val description: Int,
)
