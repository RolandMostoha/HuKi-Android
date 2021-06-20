package hu.mostoha.mobile.android.turistautak.ui.home.hikingroutes

import hu.mostoha.mobile.android.turistautak.model.ui.HikingRouteUiModel

sealed class HikingRoutesItem {
    data class Header(val text: String) : HikingRoutesItem()
    data class Item(val hikingRouteUiModel: HikingRouteUiModel) : HikingRoutesItem()
}