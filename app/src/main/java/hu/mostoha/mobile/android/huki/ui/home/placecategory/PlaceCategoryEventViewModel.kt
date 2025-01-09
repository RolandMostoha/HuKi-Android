package hu.mostoha.mobile.android.huki.ui.home.placecategory

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.util.EventSharedViewModel
import javax.inject.Inject

@HiltViewModel
class PlaceCategoryEventViewModel @Inject constructor() : EventSharedViewModel<PlaceCategoryEvent>()

sealed class PlaceCategoryEvent {

    data class PlaceCategorySelected(
        val placeCategory: PlaceCategory
    ) : PlaceCategoryEvent()

    data class LandscapeSelected(
        val landscape: LandscapeUiModel
    ) : PlaceCategoryEvent()

    data class HikingRouteSelected(
        val placeArea: PlaceArea
    ) : PlaceCategoryEvent()

}
