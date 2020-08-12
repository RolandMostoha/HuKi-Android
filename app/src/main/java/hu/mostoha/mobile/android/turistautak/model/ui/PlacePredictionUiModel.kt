package hu.mostoha.mobile.android.turistautak.model.ui

import androidx.annotation.DrawableRes

data class PlacePredictionUiModel(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?,
    @DrawableRes val iconRes: Int
) {
    override fun toString(): String {
        return primaryText
    }
}