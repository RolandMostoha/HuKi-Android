package hu.mostoha.mobile.android.turistautak.model.ui

import androidx.annotation.DrawableRes

data class HikingRouteUiModel(
    val id: String,
    val name: String,
    @DrawableRes val symbolIcon: Int
)
