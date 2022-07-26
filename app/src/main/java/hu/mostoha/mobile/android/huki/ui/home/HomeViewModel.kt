package hu.mostoha.mobile.android.huki.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val exceptionLogger: ExceptionLogger,
    private val placesInteractor: PlacesInteractor,
    private val landscapeInteractor: LandscapeInteractor,
    private val uiModelGenerator: HomeUiModelGenerator
) : ViewModel() {

    companion object {
        private const val SEARCH_QUERY_DELAY_MS = 800L
    }

    private val _mapUiModel = MutableStateFlow(MapUiModel())
    val mapUiModel: StateFlow<MapUiModel> = _mapUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, MapUiModel())

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
        viewModelScope.launch(dispatcher) {
            landscapeInteractor.requestGetLandscapesFlow()
                .map { uiModelGenerator.generateLandscapes(it) }
                .onEach { _landscapes.emit(it) }
                .catch { showError(it) }
                .collect()
        }
    }

    fun loadLandscapes(location: Location) = viewModelScope.launch(dispatcher) {
        landscapeInteractor.requestGetLandscapesFlow(location.toLocation())
            .map { uiModelGenerator.generateLandscapes(it) }
            .onEach { _landscapes.emit(it) }
            .catch { showError(it) }
            .collect()
    }

    fun loadSearchBarPlaces(searchText: String) {
        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
                viewModelScope.launch {
                    showLoading(false)
                }
            }
        }
        searchPlacesJob = viewModelScope.launch {
            placesInteractor.requestGetPlacesByFlow(searchText)
                .map { uiModelGenerator.generateSearchBarItems(it) }
                .onEach { _searchBarItems.emit(it) }
                .catch { throwable ->
                    if (throwable is DomainException) {
                        _searchBarItems.emit(uiModelGenerator.generatePlacesErrorItem(throwable))
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

    fun cancelSearch() {
        searchPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }

        viewModelScope.launch(dispatcher) {
            clearSearchBarItems()

            showLoading(false)
        }
    }

    fun loadPlace(placeUiModel: PlaceUiModel) = viewModelScope.launch(dispatcher) {
        clearSearchBarItems()
        clearFollowLocation()

        _placeDetails.emit(uiModelGenerator.generatePlaceDetails(placeUiModel))
    }

    fun loadPlaceDetails(placeUiModel: PlaceUiModel) = viewModelScope.launch(dispatcher) {
        placesInteractor.requestGeometryFlow(placeUiModel.osmId, placeUiModel.placeType)
            .map { uiModelGenerator.generatePlaceDetails(placeUiModel, it) }
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

    fun loadHikingRoutes(placeName: String, boundingBox: BoundingBox) = viewModelScope.launch(dispatcher) {
        placesInteractor.requestGetHikingRoutesFlow(boundingBox)
            .map { uiModelGenerator.generateHikingRoutes(placeName, it) }
            .onEach { _hikingRoutes.emit(it) }
            .onStart { showLoading(true) }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = viewModelScope.launch(dispatcher) {
        placesInteractor.requestGeometryFlow(hikingRoute.osmId, PlaceType.RELATION)
            .map { uiModelGenerator.generateHikingRouteDetails(hikingRoute, it) }
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

    fun updateMyLocationConfig(isFollowLocationEnabled: Boolean) = viewModelScope.launch(dispatcher) {
        _myLocationUiModel.update { it.copy(isFollowLocationEnabled = isFollowLocationEnabled) }
    }

    fun saveBoundingBox(boundingBox: BoundingBox) = viewModelScope.launch(dispatcher) {
        _mapUiModel.update { it.copy(boundingBox = boundingBox, withDefaultOffset = true) }
    }

    fun clearPlaceDetails() = viewModelScope.launch(dispatcher) {
        _placeDetails.emit(null)
    }

    fun clearHikingRoutes() = viewModelScope.launch(dispatcher) {
        _hikingRoutes.emit(null)
    }

    private fun clearFollowLocation() {
        updateMyLocationConfig(isFollowLocationEnabled = false)
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
