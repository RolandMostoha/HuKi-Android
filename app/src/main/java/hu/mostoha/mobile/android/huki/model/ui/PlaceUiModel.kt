package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.huki.model.domain.PlaceType

data class PlaceUiModel(
    val id: String,
    val placeType: PlaceType,
    val primaryText: String,
    val secondaryText: String?,
    @DrawableRes val iconRes: Int
) {
    override fun toString(): String {
        return primaryText
    }
}
