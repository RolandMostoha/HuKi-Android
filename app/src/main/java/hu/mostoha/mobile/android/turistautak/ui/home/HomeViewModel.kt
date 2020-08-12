package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.PlacesInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.model.domain.Landscape
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.PlacePredictionUiModel
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    taskExecutor: TaskExecutor,
    private val layerInteractor: LayerInteractor,
    private val placesInteractor: PlacesInteractor,
    private val landscapeRepository: LandscapeRepository,
    private val generator: HomeUiModelGenerator
) : BaseViewModel<HomeLiveEvents, HomeViewState>(taskExecutor) {

    companion object {
        const val SEARCH_QUERY_DELAY_MS = 500L
    }

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
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }
    }

    fun downloadHikingLayer() = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestDownloadHikingLayer()) {
            is TaskResult.Error -> {
                postEvent(LayerLoading(false))
                postEvent(ErrorOccurred(result.domainException.messageRes))
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
                postEvent(ErrorOccurred(result.domainException.messageRes))
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
            delay(SEARCH_QUERY_DELAY_MS)

            postEvent(SearchBarLoading(true))

            when (val result = placesInteractor.requestGetPlacesBy(searchText)) {
                is TaskResult.Success -> {
                    postEvent(SearchBarLoading(false))
                    val searchResults = generator.generatePlacesResult(result.data)
                    postEvent(PlacesResult(searchResults))
                }
                is TaskResult.Error -> {
                    postEvent(SearchBarLoading(false))
                    postEvent(ErrorOccurred(result.domainException.messageRes))
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

    fun loadLandscapes() {
        postEvent(LandscapesResult(landscapeRepository.getLandscapes()))
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
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }
    }

}

data class HomeViewState(val hikingLayerFile: File?) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(@StringRes val messageRes: Int) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class PlacesResult(val results: List<PlacePredictionUiModel>) : HomeLiveEvents()
    data class PlaceDetailsResult(val placeDetails: PlaceDetailsUiModel) : HomeLiveEvents()
    data class LandscapesResult(val landscapes: List<Landscape>) : HomeLiveEvents()
}
