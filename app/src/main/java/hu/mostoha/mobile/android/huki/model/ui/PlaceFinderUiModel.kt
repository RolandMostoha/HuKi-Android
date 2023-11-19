package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderItem

data class PlaceFinderUiModel(
    val searchText: String,
    val isLoading: Boolean,
    val staticActions: List<PlaceFinderItem.StaticActions>,
    val historyPlaces: List<PlaceFinderItem>,
    val places: List<PlaceFinderItem>,
    val error: PlaceFinderItem.Info? = null,
)
