package hu.mostoha.mobile.android.huki.ui.home

import android.location.Location
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
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.generator.HomeUiModelGenerator
import hu.mostoha.mobile.android.huki.model.ui.*
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

    private val _mapUiModel = MutableStateFlow(MapUiModel())
    val mapUiModel: StateFlow<MapUiModel> = _mapUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, MapUiModel())

    private val _hikingLayer = MutableStateFlow<HikingLayerUiModel>(HikingLayerUiModel.Loading)
    val hikingLayer: StateFlow<HikingLayerUiModel> = _hikingLayer
        .stateIn(viewModelScope, WhileViewSubscribed, HikingLayerUiModel.Loading)

    private val _myLocationUiModel = MutableStateFlow(MyLocationUiModel())
    val myLocationUiModel: StateFlow<MyLocationUiModel> = _myLocationUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, MyLocationUiModel())

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

    private val _isLoading = MutableSharedFlow<Boolean>()
    val loading: SharedFlow<Boolean> = _isLoading.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage.asSharedFlow()

    private var searchPlacesJob: Job? = null

    init {
        loadDefaultLandscapes()
    }

    fun loadHikingLayer() = launch(taskExecutor) {
        hikingLayerInteractor.requestHikingLayerFileFlow()
            .map { generator.generateHikingLayerState(it) }
            .onEach { _hikingLayer.emit(it) }
            .catch { showError(it) }
            .onStart { showLoading(true) }
            .onCompletion { showLoading(false) }
            .collect()
    }

    fun downloadHikingLayer() = launch(taskExecutor) {
        hikingLayerInteractor.requestDownloadHikingLayerFileFlow()
            .onStart { _hikingLayer.emit(HikingLayerUiModel.Downloading) }
            .catch { throwable ->
                showError(throwable)

                loadHikingLayer()
            }
            .collect()
    }

    fun saveHikingLayer(downloadId: Long) = launch(taskExecutor) {
        hikingLayerInteractor.requestSaveHikingLayerFileFlow(downloadId)
            .onStart { _hikingLayer.emit(HikingLayerUiModel.Downloading) }
            .catch { showError(it) }
            .onCompletion { loadHikingLayer() }
            .collect()
    }

    fun loadLandscapes(location: Location) = launch(taskExecutor) {
        landscapeInteractor.requestGetLandscapesFlow(location.toLocation())
            .map { generator.generateLandscapes(it) }
            .onEach { _landscapes.emit(it) }
            .catch { showError(it) }
            .collect()
    }

    fun loadSearchBarPlaces(searchText: String) = launch(taskExecutor) {
        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                showLoading(false)
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
                    showLoading(true)

                    delay(SEARCH_QUERY_DELAY_MS)
                }
                .onCompletion { showLoading(false) }
                .collect()
        }
    }

    fun cancelSearch() = launch(taskExecutor) {
        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }

        clearSearchBarItems()

        showLoading(false)
    }

    fun loadPlace(placeUiModel: PlaceUiModel) = launch(taskExecutor) {
        clearSearchBarItems()
        clearFollowLocation()

        _placeDetails.emit(generator.generatePlaceDetails(placeUiModel))
    }

    fun loadPlaceDetails(placeUiModel: PlaceUiModel) = launch(taskExecutor) {
        placesInteractor.requestGeometryFlow(placeUiModel.osmId, placeUiModel.placeType)
            .map { generator.generatePlaceDetails(placeUiModel, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart {
                clearFollowLocation()
                clearHikingRoutes()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadHikingRoutes(placeName: String, boundingBox: BoundingBox) = launch(taskExecutor) {
        placesInteractor.requestGetHikingRoutesFlow(boundingBox)
            .map { generator.generateHikingRoutes(placeName, it) }
            .onEach { _hikingRoutes.emit(it) }
            .onStart { showLoading(true) }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = launch(taskExecutor) {
        placesInteractor.requestGeometryFlow(hikingRoute.osmId, PlaceType.RELATION)
            .map { generator.generateHikingRouteDetails(hikingRoute, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart {
                clearFollowLocation()

                showLoading(true)
            }
            .onCompletion {
                clearHikingRoutes()

                showLoading(false)
            }
            .catch { showError(it) }
            .collect()
    }

    fun updateMyLocationConfig(isFollowLocationEnabled: Boolean) = launch(taskExecutor) {
        _myLocationUiModel.update { it.copy(isFollowLocationEnabled = isFollowLocationEnabled) }
    }

    fun saveBoundingBox(boundingBox: BoundingBox) = launch(taskExecutor) {
        _mapUiModel.update { it.copy(boundingBox = boundingBox, withDefaultOffset = true) }
    }

    fun clearPlaceDetails() = launch(taskExecutor) {
        _placeDetails.emit(null)
    }

    fun clearHikingRoutes() = launch(taskExecutor) {
        _hikingRoutes.emit(null)
    }

    private fun clearFollowLocation() {
        updateMyLocationConfig(isFollowLocationEnabled = false)
    }

    private fun loadDefaultLandscapes() = launch(taskExecutor) {
        landscapeInteractor.requestGetLandscapesFlow()
            .map { generator.generateLandscapes(it) }
            .onEach { _landscapes.emit(it) }
            .catch { showError(it) }
            .collect()
    }

    private suspend fun clearSearchBarItems() {
        _searchBarItems.emit(null)
    }

    private suspend fun showLoading(isLoading: Boolean) {
        _isLoading.emit(isLoading)
    }

    private suspend fun showError(throwable: Throwable) {
        if (throwable is DomainException) {
            _errorMessage.emit(throwable.messageRes)
        } else {
            Timber.e(throwable, "Unhandled exception!")

            exceptionLogger.recordException(throwable)
        }
    }

}
