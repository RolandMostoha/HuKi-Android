package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.StringRes

data class Landscape(
    val osmId: String,
    @StringRes val name: Int,
    val type: LandscapeType,
    val center: Location
)
