package hu.mostoha.mobile.android.turistautak.model.ui

import androidx.annotation.DrawableRes

data class LandscapeUiModel(
    val id: String,
    val name: String,
    @DrawableRes val icon: Int
)