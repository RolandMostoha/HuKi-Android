package hu.mostoha.mobile.android.huki.ui.home

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.architecture.BaseViewModel
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.LayerInteractor
import hu.mostoha.mobile.android.huki.interactor.PlacesInteractor
import hu.mostoha.mobile.android.huki.interactor.TaskResult
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.huki.ui.utils.toMessage
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
