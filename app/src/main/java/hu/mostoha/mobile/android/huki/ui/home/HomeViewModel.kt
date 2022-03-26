package hu.mostoha.mobile.android.huki.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.interactor.HikingLayerInteractor
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.PlacesInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.Message
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import hu.mostoha.mobile.android.huki.util.launch
import hu.mostoha.mobile.android.huki.util.launchCancellable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val exceptionLogger: ExceptionLogger,
    private val hikingLayerInteractor: HikingLayerInteractor,
    private val placesInteractor: PlacesInteractor,
    private val landscapeInteractor: LandscapeInteractor,
    private val generator: HomeUiModelGenerator
) : ViewModel() {

    companion object {
        private const val SEARCH_QUERY_DELAY_MS = 800L
    }

    private val _isLoading = MutableSharedFlow<Boolean>()
    val loading: SharedFlow<Boolean> = _isLoading

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage

    private val _hikingLayer = MutableStateFlow<HikingLayerUiModel>(HikingLayerUiModel.Loading)
    val hikingLayer: StateFlow<HikingLayerUiModel> = _hikingLayer
        .stateIn(viewModelScope, WhileViewSubscribed, HikingLayerUiModel.Loading)

    private val _placeDetails = MutableStateFlow<PlaceDetailsUiModel?>(null)
    val placeDetails: StateFlow<PlaceDetailsUiModel?> = _placeDetails
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _searchBarItems = MutableStateFlow<List<SearchBarItem>?>(null)
    val searchBarItems: StateFlow<List<SearchBarItem>?> = _searchBarItems
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _landscapes = MutableStateFlow<List<PlaceUiModel>?>(null)
    val landscapes: StateFlow<List<PlaceUiModel>?> = _landscapes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _hikingRoutes = MutableStateFlow<List<HikingRoutesItem>?>(null)
    val hikingRoutes: StateFlow<List<HikingRoutesItem>?> = _hikingRoutes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private var searchPlacesJob: Job? = null

    fun loadHikingLayer() = launch(taskExecutor) {
        hikingLayerInteractor.requestHikingLayerFileFlow()
            .map { generator.generateHikingLayerState(it) }
            .onEach { _hikingLayer.emit(it) }
            .catch { emitError(it) }
            .onStart { _isLoading.emit(true) }
            .onCompletion { _isLoading.emit(false) }
            .collect()
    }

    fun downloadHikingLayer() = launch(taskExecutor) {
        hikingLayerInteractor.requestDownloadHikingLayerFileFlow()
            .onStart { _hikingLayer.emit(HikingLayerUiModel.Downloading) }
            .catch { throwable ->
                emitError(throwable)

                loadHikingLayer()
            }
            .collect()
    }

    fun saveHikingLayer(downloadId: Long) = launch(taskExecutor) {
        hikingLayerInteractor.requestSaveHikingLayerFileFlow(downloadId)
            .onStart { _hikingLayer.emit(HikingLayerUiModel.Downloading) }
            .catch { emitError(it) }
            .onCompletion { loadHikingLayer() }
            .collect()
    }

    fun loadPlacesBy(searchText: String) = launch(taskExecutor) {
        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                _isLoading.emit(false)
            }
        }
        searchPlacesJob = launchCancellable(taskExecutor) {
            placesInteractor.requestGetPlacesByFlow(searchText)
                .map { generator.generateSearchBarItems(it) }
                .onEach { _searchBarItems.emit(it) }
                .catch { throwable ->
                    if (throwable is DomainException) {
                        _searchBarItems.emit(generator.generatePlacesErrorItem(throwable))
                    }
                }
                .onStart {
                    _isLoading.emit(true)

                    delay(SEARCH_QUERY_DELAY_MS)
                }
                .onCompletion { _isLoading.emit(false) }
                .collect()
        }
    }

    fun cancelSearch() = launch(taskExecutor) {
        _isLoading.emit(false)

        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
    }

    fun loadPlace(placeUiModel: PlaceUiModel) = launch(taskExecutor) {
        _placeDetails.emit(generator.generatePlaceDetails(placeUiModel))
    }

    fun loadPlaceDetails(placeUiModel: PlaceUiModel) = launch(taskExecutor) {
        placesInteractor.requestGeometryFlow(placeUiModel.osmId, placeUiModel.placeType)
            .map { generator.generatePlaceDetails(placeUiModel, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart { _isLoading.emit(true) }
            .onCompletion { _isLoading.emit(false) }
            .catch { emitError(it) }
            .collect()
    }

    fun loadLandscapes() = launch(taskExecutor) {
        landscapeInteractor.requestGetLandscapesFlow()
            .map { generator.generateLandscapes(it) }
            .onEach { _landscapes.emit(it) }
            .onStart { _isLoading.emit(true) }
            .onCompletion { _isLoading.emit(false) }
            .catch { emitError(it) }
            .collect()
    }

    fun loadHikingRoutes(placeName: String, boundingBox: BoundingBox) = launch(taskExecutor) {
        placesInteractor.requestGetHikingRoutesFlow(boundingBox)
            .map { generator.generateHikingRoutes(placeName, it) }
            .onEach { _hikingRoutes.emit(it) }
            .onStart { _isLoading.emit(true) }
            .onCompletion { _isLoading.emit(false) }
            .catch { emitError(it) }
            .collect()
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = launch(taskExecutor) {
        placesInteractor.requestGeometryFlow(hikingRoute.osmId, PlaceType.RELATION)
            .map { generator.generateHikingRouteDetails(hikingRoute, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart { _isLoading.emit(true) }
            .onCompletion { _isLoading.emit(false) }
            .catch { emitError(it) }
            .collect()
    }

    private suspend fun emitError(throwable: Throwable) {
        if (throwable is DomainException) {
            _errorMessage.emit(throwable.messageRes)
        } else {
            Timber.e(throwable, "Unhandled exception!")

            exceptionLogger.recordException(throwable)
        }
    }

}
