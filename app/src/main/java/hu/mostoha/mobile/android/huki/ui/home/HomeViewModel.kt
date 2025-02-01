package hu.mostoha.mobile.android.huki.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.data.LOCAL_RPDDK_ROUTES
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.mapper.HomeUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.OktRoutesMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.model.ui.CompassState
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikeModeUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.HomeEvents
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.MapConfigUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.MyLocationUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.OktRepository
import hu.mostoha.mobile.android.huki.repository.OsmPlacesRepository.Companion.OSM_PLACE_CATEGORY_QUERY_LIMIT
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import hu.mostoha.mobile.android.huki.util.distanceBetween
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val exceptionLogger: ExceptionLogger,
    private val analyticsService: AnalyticsService,
    private val placesRepository: PlacesRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val geocodingRepository: GeocodingRepository,
    private val oktRepository: OktRepository,
    private val landscapeInteractor: LandscapeInteractor,
    private val homeUiModelMapper: HomeUiModelMapper,
    private val placeDomainUiMapper: PlaceDomainUiMapper,
    private val oktRoutesMapper: OktRoutesMapper,
    private val dateTimeProvider: DateTimeProvider,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_MAP_CONFIG = "map_config"
        private const val SAVED_STATE_HIKE_MODE = "map_hike_mode"
        private const val SAVED_STATE_MY_LOCATION = "my_location"
    }

    private val savedHikeMode = savedStateHandle.get<HikeModeUiModel>(SAVED_STATE_HIKE_MODE)
    private val savedMapConfig = savedStateHandle.get<MapConfigUiModel>(SAVED_STATE_KEY_MAP_CONFIG)
    private val savedMyLocation = savedStateHandle.get<MyLocationUiModel>(SAVED_STATE_MY_LOCATION)

    private val _mapConfigUiModel = MutableStateFlow(MapConfigUiModel())
    val mapConfigUiModel: StateFlow<MapConfigUiModel> = _mapConfigUiModel
        .onEach { savedStateHandle[SAVED_STATE_KEY_MAP_CONFIG] = it }
        .stateIn(viewModelScope, WhileViewSubscribed, MapConfigUiModel())

    private val _myLocationUiModel = MutableStateFlow(MyLocationUiModel())
    val myLocationUiModel: StateFlow<MyLocationUiModel> = _myLocationUiModel
        .onEach { savedStateHandle[SAVED_STATE_MY_LOCATION] = it }
        .stateIn(viewModelScope, WhileViewSubscribed, MyLocationUiModel())

    private val _hikeModeUiModel = MutableStateFlow(HikeModeUiModel())
    val hikeModeUiModel: StateFlow<HikeModeUiModel> = _hikeModeUiModel
        .onEach { savedStateHandle[SAVED_STATE_HIKE_MODE] = it }
        .stateIn(viewModelScope, WhileViewSubscribed, HikeModeUiModel())

    private val _placeDetails = MutableStateFlow<PlaceDetailsUiModel?>(null)
    val placeDetails: StateFlow<PlaceDetailsUiModel?> = _placeDetails
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _landscapeDetails = MutableStateFlow<LandscapeDetailsUiModel?>(null)
    val landscapeDetails: StateFlow<LandscapeDetailsUiModel?> = _landscapeDetails
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _hikingRoutes = MutableStateFlow<List<HikingRoutesItem>?>(null)
    val hikingRoutes: StateFlow<List<HikingRoutesItem>?> = _hikingRoutes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _placesByCategories = MutableStateFlow<Map<PlaceCategory, List<PlaceUiModel>>>(emptyMap())
    val placesByCategories: StateFlow<Map<PlaceCategory, List<PlaceUiModel>>> = _placesByCategories
        .stateIn(viewModelScope, WhileViewSubscribed, emptyMap())

    private val _oktRoutes = MutableStateFlow<OktRoutesUiModel?>(null)
    val oktRoutes: StateFlow<OktRoutesUiModel?> = _oktRoutes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _isLoading = MutableSharedFlow<Boolean>()
    val isLoading: SharedFlow<Boolean> = _isLoading.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<Message>()
    val errorMessage: SharedFlow<Message> = _errorMessage.asSharedFlow()

    private val _homeEvents = MutableSharedFlow<HomeEvents>()
    val homeEvents: SharedFlow<HomeEvents> = _homeEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            placeHistoryRepository.clearOldPlaces()

            restoreSavedState()
        }
    }

    fun loadLandscapeDetails(landscapeUiModel: LandscapeUiModel) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getGeometry(landscapeUiModel.osmId, landscapeUiModel.osmType) },
            exceptionLogger = exceptionLogger
        )
            .map { homeUiModelMapper.mapLandscapeDetails(landscapeUiModel, it) }
            .onEach { _landscapeDetails.emit(it) }
            .onStart {
                clearFollowLocation()
                clearHikingRoutes()
                clearPlaceDetails()
                clearOktRoutes()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadLandscapeDetails(osmId: String) = viewModelScope.launch {
        landscapeInteractor.requestGetLandscapesFlow()
            .map { homeUiModelMapper.mapLandscapes(it) }
            .mapNotNull { landscapes -> landscapes.firstOrNull { it.osmId == osmId } }
            .catch { showError(it) }
            .collectLatest { loadLandscapeDetails(it) }
    }

    fun loadPlaceDetails(placeUiModel: PlaceUiModel) = viewModelScope.launch {
        clearFollowLocation()
        clearHikingRoutes()
        clearLandscapeDetails()
        clearOktRoutes()

        _placeDetails.value = placeDomainUiMapper.mapToPlaceDetailsPreview(placeUiModel)

        placeHistoryRepository.savePlace(placeUiModel, dateTimeProvider.nowInMillis())
    }

    fun loadPlaceDetailsWithGeocoding(geoPoint: GeoPoint, placeFeature: PlaceFeature) =
        viewModelScope.launch {
            when (placeFeature) {
                PlaceFeature.MAP_MY_LOCATION -> analyticsService.myLocationPlaceRequested()
                PlaceFeature.MAP_PICKED_LOCATION -> analyticsService.pickLocationPlaceRequested()
                PlaceFeature.OKT_WAYPOINT -> analyticsService.oktWaypointPlaceRequested()
                PlaceFeature.GPX_WAYPOINT -> analyticsService.gpxWaypointPlaceRequested()
                else -> Unit
            }

            flowWithExceptions(
                request = { geocodingRepository.getPlace(geoPoint.toLocation(), placeFeature) },
                exceptionLogger = exceptionLogger
            )
                .map { place ->
                    PlaceDetailsUiModel(
                        placeUiModel = placeDomainUiMapper.mapToPlaceUiModel(geoPoint, placeFeature, place),
                        geometryUiModel = GeometryUiModel.Node(geoPoint),
                    )
                }
                .onEach { placeDetailsUiModel ->
                    _placeDetails.emit(placeDetailsUiModel)

                    placeHistoryRepository.savePlace(placeDetailsUiModel.placeUiModel, dateTimeProvider.nowInMillis())
                }
                .onStart {
                    clearFollowLocation()
                    clearHikingRoutes()
                    clearLandscapeDetails()

                    showLoading(true)
                }
                .onCompletion { showLoading(false) }
                .catch { showError(it) }
                .collect()
        }

    fun loadPlaceDetailsWithGeometry(placeUiModel: PlaceUiModel) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getGeometry(placeUiModel.osmId, placeUiModel.placeType) },
            exceptionLogger = exceptionLogger
        )
            .map { placeDomainUiMapper.mapToPlaceDetailsUiModel(placeUiModel, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart {
                clearFollowLocation()
                clearHikingRoutes()
                clearLandscapeDetails()
                clearOktRoutes()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadHikingRoutes(placeArea: PlaceArea) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getHikingRoutes(placeArea.boundingBox) },
            exceptionLogger = exceptionLogger
        )
            .map { homeUiModelMapper.mapHikingRoutes(placeArea, it) }
            .onEach { _hikingRoutes.emit(it) }
            .onStart {
                clearFollowLocation()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadPlaceCategories(
        placeCategories: Set<PlaceCategory>,
        boundingBox: BoundingBox,
        refreshAll: Boolean? = false
    ) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getPlacesByCategories(placeCategories, boundingBox) },
            exceptionLogger = exceptionLogger
        )
            .map { placeDomainUiMapper.mapWithCategory(placeCategories, it) }
            .onEach { placesByCategories ->
                val numberOfPlaces = placesByCategories.flatMap { it.value }.size
                val emptyCategories = placesByCategories.filter { it.value.isEmpty() }.keys

                analyticsService.placeCategoryLoaded(numberOfPlaces)

                when {
                    numberOfPlaces >= OSM_PLACE_CATEGORY_QUERY_LIMIT -> {
                        _errorMessage.emit(
                            Message.Res(
                                res = R.string.place_category_too_many_places,
                                formatArgs = listOf(OSM_PLACE_CATEGORY_QUERY_LIMIT)
                            )
                        )
                    }
                    emptyCategories.isNotEmpty() -> {
                        _homeEvents.emit(HomeEvents.PlaceCategoryEmpty(emptyCategories))
                    }
                }

                _placesByCategories.update { actual ->
                    if (refreshAll == true) {
                        placesByCategories
                    } else {
                        actual.plus(placesByCategories)
                    }
                }
            }
            .onStart {
                clearFollowLocation()
                showLoading(true)

                if (refreshAll == true) {
                    _placesByCategories.update { it.keys.associateWith { emptyList() } }
                }
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadOsmTags(placeUiModel: PlaceUiModel) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getOsmTags(placeUiModel.osmId, placeUiModel.placeType) },
            exceptionLogger = exceptionLogger
        )
            .onEach { osmTags ->
                val tags = osmTags
                    .map { "${it.key}: ${it.value}" }
                    .joinToString("\n")

                _homeEvents.emit(HomeEvents.OsmTagsLoaded(placeUiModel.osmId, tags))
            }
            .onStart {
                clearFollowLocation()
                clearHikingRoutes()
                clearLandscapeDetails()
                clearOktRoutes()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun loadHikingRouteDetails(hikingRoute: HikingRouteUiModel) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getGeometry(hikingRoute.osmId, PlaceType.RELATION) },
            exceptionLogger = exceptionLogger
        )
            .map { placeDomainUiMapper.mapToHikingRouteDetails(hikingRoute, it) }
            .onEach { _placeDetails.emit(it) }
            .onStart {
                clearFollowLocation()
                clearPlaceDetails()
                clearLandscapeDetails()
                clearOktRoutes()

                showLoading(true)
            }
            .onCompletion {
                clearHikingRoutes()

                showLoading(false)
            }
            .catch { showError(it) }
            .collect()
    }

    fun loadOktRoutes(oktType: OktType) = viewModelScope.launch {
        flowWithExceptions(
            request = { oktRepository.getOktRoutes(oktType) },
            exceptionLogger = exceptionLogger
        )
            .map { routes ->
                when (oktType) {
                    OktType.OKT -> oktRoutesMapper.map(oktType, routes, LOCAL_OKT_ROUTES)
                    OktType.RPDDK -> oktRoutesMapper.map(oktType, routes, LOCAL_RPDDK_ROUTES)
                }
            }
            .onEach { _oktRoutes.emit(it) }
            .onStart {
                clearOktRoutes()
                clearFollowLocation()
                clearHikingRoutes()
                clearPlaceDetails()
                clearLandscapeDetails()

                showLoading(true)
            }
            .onCompletion { showLoading(false) }
            .catch { showError(it) }
            .collect()
    }

    fun selectOktRoute(oktId: String) {
        _oktRoutes.update { oktRoutesUiModel ->
            if (oktRoutesUiModel == null) {
                return@update null
            }

            oktRoutesUiModel.copy(
                routes = oktRoutesUiModel.routes.map { route ->
                    route.copy(isSelected = route.oktId == oktId)
                }
            )
        }
    }

    fun selectOktRoute(geoPoint: GeoPoint) {
        val oktRoutes = oktRoutes.value ?: return
        val selectedOktId = oktRoutes.routes
            .drop(1)
            .map { oktRoute ->
                val closestPoint = oktRoute.geoPoints.minBy { it.toLocation().distanceBetween(geoPoint.toLocation()) }

                oktRoute.oktId to closestPoint
            }
            .minBy { it.second.toLocation().distanceBetween(geoPoint.toLocation()) }
            .first

        selectOktRoute(selectedOktId)
    }

    fun updateMyLocationConfig(
        isLocationPermissionEnabled: Boolean? = null,
        isFollowLocationEnabled: Boolean? = null,
    ) {
        _myLocationUiModel.update { uiModel ->
            uiModel.copy(
                isLocationPermissionEnabled = isLocationPermissionEnabled ?: uiModel.isLocationPermissionEnabled,
                isFollowLocationEnabled = isFollowLocationEnabled ?: uiModel.isFollowLocationEnabled,
            )
        }
    }

    fun toggleHikeMode() {
        _hikeModeUiModel.update { uiModel ->
            val isHikeModeEnabled = !uiModel.isHikeModeEnabled
            val isPermissionEnabled = myLocationUiModel.value.isLocationPermissionEnabled

            if (isPermissionEnabled) {
                updateMyLocationConfig(isFollowLocationEnabled = isHikeModeEnabled)
            }

            val compassState = if (isPermissionEnabled && isHikeModeEnabled) {
                CompassState.Live
            } else {
                CompassState.North
            }

            uiModel.copy(
                isHikeModeEnabled = isHikeModeEnabled,
                compassState = compassState,
            )
        }
    }

    fun disableHikeMode() {
        _hikeModeUiModel.update { it.copy(isHikeModeEnabled = false) }
    }

    fun toggleLiveCompass(mapOrientation: Float) {
        val isPermissionEnabled = myLocationUiModel.value.isLocationPermissionEnabled

        val newCompassSate = when (_hikeModeUiModel.value.compassState) {
            is CompassState.North -> {
                if (isPermissionEnabled) {
                    CompassState.Live
                } else {
                    CompassState.Free(mapOrientation)
                }
            }
            is CompassState.Live -> CompassState.Free(mapOrientation)
            is CompassState.Free -> CompassState.North
        }

        updateMyLocationConfig(isFollowLocationEnabled = isPermissionEnabled && newCompassSate == CompassState.Live)

        _hikeModeUiModel.update { it.copy(compassState = newCompassSate) }
    }

    fun setFreeCompass(mapOrientation: Float) {
        _hikeModeUiModel.update { it.copy(compassState = CompassState.Free(mapOrientation)) }
    }

    fun saveMapBoundingBox(boundingBox: BoundingBox) {
        _mapConfigUiModel.update { it.copy(boundingBox = boundingBox) }
    }

    fun clearPlaceDetails() {
        _placeDetails.value = null
    }

    fun clearLandscapeDetails() {
        _landscapeDetails.value = null
    }

    fun clearHikingRoutes() {
        _hikingRoutes.value = null
    }

    fun clearOktRoutes() {
        _oktRoutes.value = null
    }

    fun clearFollowLocation() {
        updateMyLocationConfig(isFollowLocationEnabled = false)
    }

    fun clearPlaceCategory(placeCategory: PlaceCategory) {
        _placesByCategories.update { placesForCategories ->
            placesForCategories.minus(placeCategory)
        }
    }

    fun clearPlaceCategories() {
        _placesByCategories.value = emptyMap()
    }

    fun clearAllOverlay() {
        clearPlaceDetails()
        clearLandscapeDetails()
        clearHikingRoutes()
        clearOktRoutes()
        clearFollowLocation()
        clearPlaceCategories()
    }

    private suspend fun showLoading(isLoading: Boolean) {
        _isLoading.emit(isLoading)
    }

    private suspend fun showError(throwable: Throwable) {
        Timber.e(throwable)

        if (throwable is DomainException) {
            _errorMessage.emit(throwable.messageRes)
        } else {
            exceptionLogger.recordException(throwable)
        }
    }

    private suspend fun restoreSavedState() {
        if (savedHikeMode != null) {
            _hikeModeUiModel.emit(savedHikeMode)
        }
        if (savedMapConfig != null) {
            _mapConfigUiModel.emit(savedMapConfig)
        }
        if (savedMyLocation != null) {
            _myLocationUiModel.emit(savedMyLocation)
        }
    }

}
