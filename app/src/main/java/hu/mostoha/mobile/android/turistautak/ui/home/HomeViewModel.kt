package hu.mostoha.mobile.android.turistautak.ui.home

import androidx.annotation.StringRes
import androidx.hilt.lifecycle.ViewModelInject
import hu.mostoha.mobile.android.turistautak.architecture.BaseViewModel
import hu.mostoha.mobile.android.turistautak.architecture.LiveEvents
import hu.mostoha.mobile.android.turistautak.architecture.ViewState
import hu.mostoha.mobile.android.turistautak.domain.model.Landscape
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import hu.mostoha.mobile.android.turistautak.interactor.LayerInteractor
import hu.mostoha.mobile.android.turistautak.interactor.OverpassInteractor
import hu.mostoha.mobile.android.turistautak.interactor.TaskResult
import hu.mostoha.mobile.android.turistautak.repository.LandscapeRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchBarUiModelGenerator
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchResultItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File

class HomeViewModel @ViewModelInject constructor(
    taskExecutor: TaskExecutor,
    private val layerInteractor: LayerInteractor,
    private val overpassInteractor: OverpassInteractor,
    private val landscapeRepository: LandscapeRepository,
    private val generator: SearchBarUiModelGenerator
) : BaseViewModel<HomeLiveEvents, HomeViewState>(taskExecutor) {

    companion object {
        const val SEARCH_QUERY_DELAY_MS = 500L
    }

    private var searchJob: Job? = null

    fun checkHikingLayer() = launch {
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

    fun handleFileDownloaded(downloadId: Long) = launch {
        postEvent(LayerLoading(true))

        when (val result = layerInteractor.requestSaveHikingLayer(downloadId)) {
            is TaskResult.Success -> {
                checkHikingLayer()
            }
            is TaskResult.Error -> {
                postEvent(ErrorOccurred(result.domainException.messageRes))
            }
        }
    }

    fun searchHikingRelationsBy(searchText: String) {
        searchJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                postEvent(SearchBarLoading(false))
            }
        }
        searchJob = launchCancellable {
            delay(SEARCH_QUERY_DELAY_MS)

            postEvent(SearchBarLoading(true))

            when (val result = overpassInteractor.requestSearchHikingRelationsBy(searchText)) {
                is TaskResult.Success -> {
                    postEvent(SearchBarLoading(false))
                    val searchResults = generator.generate(result.data.elements)
                    postEvent(SearchResult(searchResults))
                }
                is TaskResult.Error -> {
                    postEvent(SearchBarLoading(false))
                    postEvent(ErrorOccurred(result.domainException.messageRes))
                }
            }
        }
    }

    fun initLandscapes() {
        postEvent(LandscapesResult(landscapeRepository.getLandscapes()))
    }

}

data class HomeViewState(val hikingLayerFile: File?) : ViewState

sealed class HomeLiveEvents : LiveEvents {
    data class ErrorOccurred(@StringRes val messageRes: Int) : HomeLiveEvents()
    data class LayerLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class SearchBarLoading(val inProgress: Boolean) : HomeLiveEvents()
    data class SearchResult(val results: List<SearchResultItem>) : HomeLiveEvents()
    data class LandscapesResult(val landscapes: List<Landscape>) : HomeLiveEvents()
}
