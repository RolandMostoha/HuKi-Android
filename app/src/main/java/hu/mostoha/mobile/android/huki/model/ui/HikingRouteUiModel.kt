package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.DrawableRes

data class HikingRouteUiModel(
    val osmId: String,
    val name: String,
    @DrawableRes val symbolIcon: Int
)
