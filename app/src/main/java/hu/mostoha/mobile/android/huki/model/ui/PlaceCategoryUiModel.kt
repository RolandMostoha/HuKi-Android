package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.Destination

data class PlaceCategoryUiModel(
    val isAreaLoading: Boolean = true,
    val placeArea: PlaceArea? = null,
    val destinations: List<Destination> = emptyList(),
)
