package hu.mostoha.mobile.android.huki.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
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
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.MapUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.MyLocationUiModel
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.OktRepository
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
import kotlinx.coroutines.flow.map
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

    private val _mapUiModel = MutableStateFlow(MapUiModel())
    val mapUiModel: StateFlow<MapUiModel> = _mapUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, MapUiModel())

    private val _myLocationUiModel = MutableStateFlow(MyLocationUiModel())
    val myLocationUiModel: StateFlow<MyLocationUiModel> = _myLocationUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, MyLocationUiModel())

    private val _hikeModeUiModel = MutableStateFlow(HikeModeUiModel())
    val hikeModeUiModel: StateFlow<HikeModeUiModel> = _hikeModeUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, HikeModeUiModel())

    private val _placeDetails = MutableStateFlow<PlaceDetailsUiModel?>(null)
    val placeDetails: StateFlow<PlaceDetailsUiModel?> = _placeDetails
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _landscapes = MutableStateFlow<List<LandscapeUiModel>?>(null)
    val landscapes: StateFlow<List<LandscapeUiModel>?> = _landscapes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _landscapeDetails = MutableStateFlow<LandscapeDetailsUiModel?>(null)
    val landscapeDetails: StateFlow<LandscapeDetailsUiModel?> = _landscapeDetails
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _hikingRoutes = MutableStateFlow<List<HikingRoutesItem>?>(null)
    val hikingRoutes: StateFlow<List<HikingRoutesItem>?> = _hikingRoutes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _oktRoutes = MutableStateFlow<OktRoutesUiModel?>(null)
    val oktRoutes: StateFlow<OktRoutesUiModel?> = _oktRoutes
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _isLoading = MutableSharedFlow<Boolean>()
    val loading: SharedFlow<Boolean> = _isLoading.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            landscapeInteractor.requestGetLandscapesFlow()
                .map { homeUiModelMapper.mapLandscapes(it) }
                .onEach { _landscapes.emit(it) }
                .catch { showError(it) }
                .collect()

            placeHistoryRepository.clearOldPlaces()
        }
    }

    fun loadLandscapes(location: Location) = viewModelScope.launch {
        landscapeInteractor.requestGetLandscapesFlow(location.toLocation())
            .map { homeUiModelMapper.mapLandscapes(it) }
            .onEach { _landscapes.emit(it) }
            .catch { showError(it) }
            .collect()
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

    fun loadHikingRoutes(placeName: String, boundingBox: BoundingBox) = viewModelScope.launch {
        flowWithExceptions(
            request = { placesRepository.getHikingRoutes(boundingBox) },
            exceptionLogger = exceptionLogger
        )
            .map { homeUiModelMapper.mapHikingRoutes(placeName, it) }
            .onEach { _hikingRoutes.emit(it) }
            .onStart {
                clearFollowLocation()

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

    fun loadOktRoutes() = viewModelScope.launch {
        flowWithExceptions(
            request = { oktRepository.getOktRoutes() },
            exceptionLogger = exceptionLogger
        )
            .map { oktRoutesMapper.map(it, LOCAL_OKT_ROUTES) }
            .onEach { _oktRoutes.emit(it) }
            .onStart {
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
        _myLocationUiModel.update { locationUiModel ->
            var model = locationUiModel

            isLocationPermissionEnabled?.let { model = model.copy(isLocationPermissionEnabled = it) }
            isFollowLocationEnabled?.let { model = model.copy(isFollowLocationEnabled = it) }

            model
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

    fun disableFollowLocation() {
        updateMyLocationConfig(isFollowLocationEnabled = false)
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

    fun saveMapConfig(boundingBox: BoundingBox) {
        _mapUiModel.update { it.copy(boundingBox = boundingBox) }
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

    fun clearAllOverlay() {
        clearPlaceDetails()
        clearLandscapeDetails()
        clearHikingRoutes()
        clearOktRoutes()
        clearFollowLocation()
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

}
