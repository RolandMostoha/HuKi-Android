package hu.mostoha.mobile.android.huki.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.architecture.LiveEvents
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.utils.Message

sealed class HomeLiveEvents : LiveEvents {

    data class ErrorOccurred(val message: Message) : HomeLiveEvents()

    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()

    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()

    data class PlacesResult(val results: List<PlaceUiModel>) : HomeLiveEvents()

    data class PlacesErrorResult(
        @StringRes val messageRes: Int,
        @DrawableRes val drawableRes: Int
    ) : HomeLiveEvents()

    data class PlaceDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()

    data class LandscapesResult(val landscapes: List<PlaceUiModel>) : HomeLiveEvents()

    data class HikingRoutesResult(val hikingRoutes: List<HikingRoutesItem>) : HomeLiveEvents()

    data class HikingRouteDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()

}
