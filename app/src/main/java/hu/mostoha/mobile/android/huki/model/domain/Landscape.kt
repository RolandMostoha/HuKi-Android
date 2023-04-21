package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.StringRes

data class Landscape(
    val osmId: String,
    val osmType: PlaceType,
    @StringRes val nameRes: Int,
    val landscapeType: LandscapeType,
    val center: Location,
    val kirandulastippekTag: String? = null,
)
