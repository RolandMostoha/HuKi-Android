package hu.mostoha.mobile.android.huki.ui.home.hikingroutes

import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea

sealed class HikingRoutesItem {

    data class Header(val placeArea: PlaceArea) : HikingRoutesItem()

    data class Item(val hikingRouteUiModel: HikingRouteUiModel) : HikingRoutesItem()

    data object Empty : HikingRoutesItem()

}
