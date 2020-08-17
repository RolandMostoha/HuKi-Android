package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.PlacesInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import hu.mostoha.mobile.android.turistautak.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.turistautak.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlacePredictionUiModel
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.utils.Message
import hu.mostoha.mobile.android.turistautak.ui.utils.toMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File

class HomeViewModel @ViewModelInject constructor(
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
                postState(HomeViewState(result.data))
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
                        // TODO
                    } else {
                        postEvent(PlacesResult(searchResults))
                    }
                }
                is TaskResult.Error -> {
                    postEvent(SearchBarLoading(false))
                    postEvent(ErrorOccurred(result.domainException.messageRes.toMessage()))
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

    fun loadPlaceDetails(id: String, placeType: PlaceType) = launch {
        postEvent(SearchBarLoading(true))

        when (val result = placesInteractor.requestGetGetPlaceDetails(id, placeType)) {
            is TaskResult.Success -> {
                postEvent(SearchBarLoading(false))
                val placeDetails = generator.generatePlaceDetails(result.data)
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

}

data class HomeViewState(val hikingLayerFile: File?) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(val message: Message) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class PlacesResult(val results: List<PlacePredictionUiModel>) : HomeLiveEvents()
    data class PlaceDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()
    data class LandscapesResult(val landscapes: List<LandscapeUiModel>) : HomeLiveEvents()
}
