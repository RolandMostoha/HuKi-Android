package hu.mostoha.mobile.android.huki.ui.home

import hu.mostoha.mobile.android.huki.architecture.LiveEvents
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.Message

sealed class HomeLiveEvents : LiveEvents {

    data class ErrorResult(val message: Message.Res) : HomeLiveEvents()

    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()

    data class SearchBarResult(val searchBarItems: List<SearchBarItem>) : HomeLiveEvents()

    data class PlaceDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()

    data class LandscapesResult(val landscapes: List<PlaceUiModel>) : HomeLiveEvents()

    data class HikingRoutesResult(val hikingRoutes: List<HikingRoutesItem>) : HomeLiveEvents()

}
