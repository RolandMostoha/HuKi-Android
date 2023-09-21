package hu.mostoha.mobile.android.huki.ui.home.routeplanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.DefaultDispatcher
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.swap
import hu.mostoha.mobile.android.huki.extensions.update
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.mapper.RoutePlannerUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RoutePlannerViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val exceptionLogger: ExceptionLogger,
    private val routePlannerRepository: RoutePlannerRepository,
    private val routePlannerUiModelMapper: RoutePlannerUiModelMapper,
    private val myLocationProvider: AsyncMyLocationProvider,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _routePlanUiModel = MutableStateFlow<RoutePlanUiModel?>(null)
    val routePlanUiModel: StateFlow<RoutePlanUiModel?> = _routePlanUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    private val _wayPointItems = MutableStateFlow(emptyList<WaypointItem>())
    val waypointItems: StateFlow<List<WaypointItem>> = _wayPointItems
        .stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    private val _routePlanGpxFileUri = MutableSharedFlow<Uri>()
    val routePlanGpxFileUri: SharedFlow<Uri> = _routePlanGpxFileUri.asSharedFlow()

    private val _isRoutePlanLoading = MutableSharedFlow<Boolean>()
    val isRoutePlanLoading: SharedFlow<Boolean> = _isRoutePlanLoading.asSharedFlow()

    private val _routePlanErrorMessage = MutableSharedFlow<Message.Res?>()
    val routePlanErrorMessage: SharedFlow<Message.Res?> = _routePlanErrorMessage.asSharedFlow()

    init {
        viewModelScope.launch(defaultDispatcher) {
            waypointItems
                .flatMapLatest { waypoints ->
                    val triggerLocations = waypoints.mapNotNull { it.location }
                    if (triggerLocations.size < 2) {
                        return@flatMapLatest emptyFlow()
                    }

                    val lastTriggerLocations = routePlanUiModel.value?.triggerLocations
                    if (lastTriggerLocations != null && triggerLocations == lastTriggerLocations) {
                        return@flatMapLatest emptyFlow()
                    }

                    flowWithExceptions(
                        request = { routePlannerRepository.getRoutePlan(triggerLocations) },
                        exceptionLogger = exceptionLogger
                    )
                        .map { routePlannerUiModelMapper.mapToRoutePlanUiModel(waypoints, triggerLocations, it) }
                        .onEach { _routePlanUiModel.emit(it) }
                        .onStart {
                            _routePlanErrorMessage.emit(null)
                            _isRoutePlanLoading.emit(true)
                        }
                        .onCompletion { _isRoutePlanLoading.emit(false) }
                        .catch { throwable ->
                            val domainException = DomainException(
                                Message.Res(R.string.route_planner_general_error_message),
                                throwable
                            )
                            showError(domainException)
                        }
                        .flowOn(ioDispatcher)
                }
                .catch { showError(it) }
                .collect()
        }
    }

    fun initWaypoints() {
        val waypoints = listOf(
            WaypointItem(order = 0, waypointType = WaypointType.START),
            WaypointItem(order = 1, waypointType = WaypointType.END)
        )

        _wayPointItems.value = waypoints
    }

    fun initWaypoint(placeUiModel: PlaceUiModel) {
        _wayPointItems.value = listOf(
            WaypointItem(
                order = 0,
                waypointType = WaypointType.START
            ),
            WaypointItem(
                order = 1,
                waypointType = WaypointType.END,
                primaryText = placeUiModel.primaryText,
                location = placeUiModel.geoPoint.toLocation(),
            )
        )
    }

    fun initWaypoints(locations: List<Pair<Message, GeoPoint>>) {
        _wayPointItems.value = locations.mapIndexed { index, (name, geoPoint) ->
            WaypointItem(
                order = index,
                waypointType = when (index) {
                    0 -> WaypointType.START
                    locations.lastIndex -> WaypointType.END
                    else -> WaypointType.INTERMEDIATE
                },
                primaryText = name,
                location = geoPoint.toLocation(),
            )
        }
    }

    fun updateWaypoint(waypointItem: WaypointItem, name: Message, location: Location, searchText: String) {
        _wayPointItems.update { wayPointItemList ->
            wayPointItemList.update(waypointItem) { wayPointItem ->
                wayPointItem.copy(
                    primaryText = name,
                    location = location,
                    searchText = searchText
                )
            }
        }
    }

    fun updateWaypointWithMyLocation(waypointItem: WaypointItem, name: Message, searchText: String) {
        viewModelScope.launch(defaultDispatcher) {
            val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()
            if (lastKnownLocation == null) {
                _routePlanErrorMessage.emit(Message.Res(R.string.place_finder_my_location_error_null_location))
                return@launch
            }

            _wayPointItems.update { wayPointItemList ->
                wayPointItemList.update(waypointItem) { wayPointItem ->
                    wayPointItem.copy(
                        primaryText = name,
                        location = lastKnownLocation.toLocation(),
                        searchText = searchText
                    )
                }
            }
        }
    }

    fun addEmptyWaypoint() {
        _wayPointItems.update { wayPointItemList ->
            val emptyWaypoint = WaypointItem(
                order = wayPointItemList.size - 1,
                waypointType = WaypointType.END
            )
            wayPointItemList
                .plus(emptyWaypoint)
                .reOrder()
        }
    }

    fun removeWaypoint(waypointItem: WaypointItem) {
        _wayPointItems.update { wayPointItemList ->
            wayPointItemList
                .minus(waypointItem)
                .reOrder()
        }
    }

    fun swapWaypoints(fromWaypoint: WaypointItem, toWaypoint: WaypointItem) {
        _wayPointItems.update { wayPointItemList ->
            wayPointItemList
                .swap(fromWaypoint, toWaypoint)
                .reOrder()
        }
    }

    fun createRoundTrip() {
        if (waypointItems.value.size >= 2) {
            _wayPointItems.update { wayPointItemList ->
                wayPointItemList
                    .plus(wayPointItemList.first())
                    .reOrder()
            }
        }
    }

    fun saveRoutePlan() {
        viewModelScope.launch(ioDispatcher) {
            val routePlan = _routePlanUiModel.value ?: return@launch

            analyticsService.routePlanSaved(routePlan)

            val fileUri = routePlannerRepository.saveRoutePlan(routePlan)

            if (fileUri != null) {
                _routePlanGpxFileUri.emit(fileUri)
            } else {
                showError(DomainException(R.string.route_planner_error_file_save_unsuccsessful.toMessage()))
            }
        }
    }

    fun clearRoutePlanner() {
        _routePlanUiModel.value = null
        _wayPointItems.value = emptyList()
    }

    private fun List<WaypointItem>.reOrder(): List<WaypointItem> {
        val orderedWaypoints = this.mapIndexed { index, waypointItem ->
            waypointItem.copy(order = index)
        }

        return orderedWaypoints.mapIndexed { index, waypointItem ->
            val newWaypointType = when (index) {
                0 -> WaypointType.START
                orderedWaypoints.lastIndex -> WaypointType.END
                else -> WaypointType.INTERMEDIATE
            }

            waypointItem.copy(waypointType = newWaypointType)
        }
    }

    private suspend fun showError(throwable: Throwable) {
        Timber.e(throwable)

        if (throwable is DomainException) {
            _routePlanErrorMessage.emit(throwable.messageRes)
        } else {
            exceptionLogger.recordException(throwable)
        }
    }

}
