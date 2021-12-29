package hu.mostoha.mobile.android.huki.ui.home

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.architecture.BaseViewModel
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.interactor.HikingLayerInteractor
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.PlacesInteractor
import hu.mostoha.mobile.android.huki.interactor.TaskResult
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerState
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.home.HomeLiveEvents.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    taskExecutor: TaskExecutor,
    private val hikingLayerInteractor: HikingLayerInteractor,
    private val placesInteractor: PlacesInteractor,
    private val landscapeInteractor: LandscapeInteractor,
    private val generator: HomeUiModelGenerator
) : BaseViewModel<HomeLiveEvents, HomeViewState>(taskExecutor) {

    companion object {
        private const val SEARCH_QUERY_DELAY_MS = 800L
    }

    private var searchJob: Job? = null

    fun loadHikingLayer() = launch {
        when (val result = hikingLayerInteractor.requestGetHikingLayerFile()) {
            is TaskResult.Success -> {
                postState(HomeViewState(generator.generateHikingLayerState(result.data)))
            }
            is TaskResult.Error -> {
                postState(HomeViewState(HikingLayerState.NotDownloaded))
                postEvent(ErrorResult(result.domainException.messageRes))
            }
        }
    }

    fun downloadHikingLayer() = launch {
        postState(HomeViewState(HikingLayerState.Downloading))

        when (val result = hikingLayerInteractor.requestDownloadHikingLayerFile()) {
            is TaskResult.Error -> {
                postEvent(ErrorResult(result.domainException.messageRes))
                loadHikingLayer()
            }
            is TaskResult.Success -> {
                // No-op, successfully downloaded layer will be handled via LocalBroadcast
            }
        }
    }

    fun loadDownloadedFile(downloadId: Long) = launch {
        postState(HomeViewState(HikingLayerState.Downloading))

        val result = hikingLayerInteractor.requestSaveHikingLayerFile(downloadId)

        if (result is TaskResult.Error) {
            postEvent(ErrorResult(result.domainException.messageRes))
        }

        loadHikingLayer()
    }

    fun loadPlacesBy(searchText: String) {
        searchJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                postEvent(SearchBarLoading(false))
            }
        }
        searchJob = launchCancellable {
            delay(SEARCH_QUERY_DELAY_MS)

            postEvent(SearchBarLoading(true))

            when (val result = placesInteractor.requestGetPlacesBy(searchText)) {
                is TaskResult.Success -> {
                    postEvent(SearchBarLoading(false))
                    val searchResults = generator.generatePlaceAdapterItems(result.data)
                    if (searchResults.isEmpty()) {
                        postEvent(SearchBarResult(generator.generatePlacesEmptyItem()))
                    } else {
                        postEvent(SearchBarResult(searchResults))
                    }
                }
                is TaskResult.Error -> {
                    postEvent(SearchBarLoading(false))
                    postEvent(SearchBarResult(generator.generatePlacesErrorItem(result.domainException)))
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

    fun loadPlace(placeUiModel: PlaceUiModel) = launch {
        postEvent(PlaceDetailsResult(generator.generatePlaceDetails(placeUiModel)))
    }

    fun loadPlaceDetails(placeUiModel: PlaceUiModel) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetGeometry(placeUiModel.osmId, placeUiModel.placeType)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val placeDetails = generator.generatePlaceDetails(placeUiModel, result.data)
                postEvent(PlaceDetailsResult(placeDetails))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorResult(result.domainException.messageRes))
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
                postEvent(ErrorResult(result.domainException.messageRes))
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
                postEvent(ErrorResult(result.domainException.messageRes))
            }
        }
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetGeometry(hikingRoute.osmId, PlaceType.RELATION)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val placeDetails = generator.generateHikingRouteDetails(hikingRoute, result.data)
                postEvent(PlaceDetailsResult(placeDetails))
            }
            is TaskResult.Error -> {
                postEvent(SearchBarLoading(false))
                postEvent(ErrorResult(result.domainException.messageRes))
            }
        }
    }

}
