package hu.mostoha.mobile.android.huki.ui.home.hikingroutes

import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel

sealed class HikingRoutesItem {

    data class Header(val title: String) : HikingRoutesItem()

    data class Item(val hikingRouteUiModel: HikingRouteUiModel) : HikingRoutesItem()

    object Empty : HikingRoutesItem()

}
