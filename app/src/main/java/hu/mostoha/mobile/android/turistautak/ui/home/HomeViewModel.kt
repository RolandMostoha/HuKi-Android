package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.PlacesInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.model.domain.BoundingBox
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import hu.mostoha.mobile.android.turistautak.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.turistautak.model.ui.HikingLayerDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.turistautak.ui.utils.Message
import hu.mostoha.mobile.android.turistautak.ui.utils.toMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    taskExecutor: TaskExecutor,
    private val layerInteractor: LayerInteractor,
    private val placesInteractor: PlacesInteractor,
    private val landscapeInteractor: LandscapeInteractor,
    private val generator: HomeUiModelGenerator
) : BaseViewModel<HomeLiveEvents, HomeViewState>(taskExecutor) {

    private var searchJob: Job? = null

    fun loadHikingLayer() = launch {
        postEvent(LayerLoading(true))

        val result = layerInteractor.requestGetHikingLayer()

        postEvent(LayerLoading(false))
        when (result) {
            is TaskResult.Success -> {
                postState(HomeViewState(generator.generateHikingLayerDetails(result.data)))
            }
            is TaskResult.Error -> {
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun downloadHikingLayer() = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestDownloadHikingLayer()) {
            is TaskResult.Error -> {
                postEvent(LayerLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun loadDownloadedFile(downloadId: Long) = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestSaveHikingLayer(downloadId)) {
            is TaskResult.Success -> {
                loadHikingLayer()
            }
            is TaskResult.Error -> {
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun loadPlacesBy(searchText: String) {
        searchJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                postEvent(SearchBarLoading(false))
            }
        }
        searchJob = launchCancellable {
            delay(NetworkConfig.SEARCH_QUERY_DELAY_MS)

            postEvent(SearchBarLoading(true))

            when (val result = placesInteractor.requestGetPlacesBy(searchText)) {
                is TaskResult.Success -> {
                    postEvent(SearchBarLoading(false))
                    val searchResults = generator.generatePlacesResult(result.data)
                    if (searchResults.isEmpty()) {
                        postEvent(
                            PlacesErrorResult(
                                R.string.search_bar_empty_message, R.drawable.ic_search_bar_empty_result
                            )
                        )
                    } else {
                        postEvent(PlacesResult(searchResults))
                    }
                }
                is TaskResult.Error -> {
                    postEvent(SearchBarLoading(false))
                    postEvent(
                        PlacesErrorResult(
                            result.domainException.messageRes, R.drawable.ic_search_bar_error
                        )
                    )
                }
            }
        }
    }

    fun cancelSearch() {
        postEvent(SearchBarLoading(false))

        searchJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
    }

    fun loadPlaceDetails(place: PlaceUiModel) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetPlaceDetails(place.id, place.placeType)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val placeDetails = generator.generatePlaceDetails(place, result.data)
                postEvent(PlaceDetailsResult(placeDetails))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun loadLandscapes() = launch {
        postEvent(SearchBarLoading(true))

        when (val result = landscapeInteractor.requestGetLandscapes()) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val landscapes = generator.generateLandscapes(result.data)
                postEvent(LandscapesResult(landscapes))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun loadHikingRoutes(placeName: String, boundingBox: BoundingBox) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetHikingRoutes(boundingBox)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val hikingRoutes = generator.generateHikingRoutes(placeName, result.data)
                postEvent(HikingRoutesResult(hikingRoutes))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetPlaceDetails(hikingRoute.id, PlaceType.RELATION)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val placeDetails = generator.generateHikingRouteDetails(hikingRoute, result.data)
                postEvent(HikingRouteDetailsResult(placeDetails))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
            }
        }
    }

}

data class HomeViewState(val hikingLayerDetails: HikingLayerDetailsUiModel) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(val message: Message) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class PlacesResult(val results: List<PlaceUiModel>) : HomeLiveEvents()
    data class PlacesErrorResult(@StringRes val messageRes: Int, @DrawableRes val drawableRes: Int) : HomeLiveEvents()
    data class PlaceDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()
    data class LandscapesResult(val landscapes: List<PlaceUiModel>) : HomeLiveEvents()
    data class HikingRoutesResult(val hikingRoutes: List<HikingRoutesItem>) : HomeLiveEvents()
    data class HikingRouteDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()
}
