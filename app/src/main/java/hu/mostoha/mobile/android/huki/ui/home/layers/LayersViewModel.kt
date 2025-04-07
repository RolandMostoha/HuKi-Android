package hu.mostoha.mobile.android.huki.ui.home.layers

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.LayersConfig
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.model.mapper.LayersUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import hu.mostoha.mobile.android.huki.util.calculateDirectionArrows
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LayersViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val layersRepository: LayersRepository,
    private val settingsRepository: SettingsRepository,
    private val layersUiModelMapper: LayersUiModelMapper,
    private val hikingTileUrlProvider: HikingTileUrlProvider,
    private val exceptionLogger: ExceptionLogger,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    companion object {
        private const val SAVED_STATE_KEY_GPX_FILE_URI = "gpx_file_uri"
    }

    private val savedFileUri = savedStateHandle.get<String>(SAVED_STATE_KEY_GPX_FILE_URI)

    private lateinit var hikingLayerZoomRanges: List<TileZoomRange>

    private val baseLayer = settingsRepository.getBaseLayer()
        .stateIn(viewModelScope, WhileViewSubscribed, BaseLayer.MAPNIK)

    private val hikingLayer = MutableStateFlow<HikingLayer?>(null)

    val layersConfig = baseLayer.combine(hikingLayer) { baseLayer, hikingLayer ->
        LayersConfig(baseLayer, hikingLayer)
    }.stateIn(viewModelScope, WhileViewSubscribed, LayersConfig(baseLayer = BaseLayer.MAPNIK, hikingLayer = null))

    private val _gpxDetailsUiModel = MutableStateFlow<GpxDetailsUiModel?>(null)
    val gpxDetailsUiModel: StateFlow<GpxDetailsUiModel?> = _gpxDetailsUiModel
        .onEach { savedStateHandle[SAVED_STATE_KEY_GPX_FILE_URI] = it?.fileUri }
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    val layerAdapterItems = layersConfig.combine(gpxDetailsUiModel) { layersConfig, gpxDetails ->
        layersUiModelMapper.mapLayerAdapterItems(layersConfig.baseLayer, layersConfig.hikingLayer, gpxDetails)
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    private val _errorMessage = MutableStateFlow<Message.Res?>(null)
    val errorMessage: SharedFlow<Message.Res?> = _errorMessage
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    init {
        viewModelScope.launch {
            flowWithExceptions(
                request = { layersRepository.getHikingLayerZoomRanges() },
                exceptionLogger = exceptionLogger
            )
                .onEach { zoomRanges ->
                    hikingLayerZoomRanges = zoomRanges

                    hikingLayer.emit(getHikingLayerSpec(zoomRanges))
                }
                .catch { Timber.e(it) }
                .collect()

            restoreSavedState()
        }
    }

    fun loadGpx(uri: Uri?) = viewModelScope.launch(ioDispatcher) {
        flowWithExceptions(
            request = { layersRepository.getGpxDetails(uri) },
            exceptionLogger = exceptionLogger
        )
            .onEach { gpxDetails ->
                val isGpxSlopeEnabled = settingsRepository.isGpxSlopeColoringEnabled().firstOrNull() ?: true
                val gpxDetailsUiModel = layersUiModelMapper.mapGpxDetails(gpxDetails, isGpxSlopeEnabled)

                analyticsService.gpxImported(gpxDetailsUiModel.name)

                _gpxDetailsUiModel.emit(gpxDetailsUiModel)
            }
            .catch { showError(it) }
            .collect()
    }

    fun loadRoutePlannerGpx(uri: Uri) = viewModelScope.launch(ioDispatcher) {
        flowWithExceptions(
            request = { layersRepository.getRoutePlannerGpxDetails(uri) },
            exceptionLogger = exceptionLogger
        )
            .onEach { gpxDetails ->
                val isGpxSlopeEnabled = settingsRepository.isGpxSlopeColoringEnabled().firstOrNull() ?: true
                val gpxDetailsUiModel = layersUiModelMapper.mapGpxDetails(gpxDetails, isGpxSlopeEnabled)

                _gpxDetailsUiModel.emit(gpxDetailsUiModel)
            }
            .catch { showError(it) }
            .collect()
    }

    fun selectBaseLayer(layer: BaseLayer) = viewModelScope.launch(ioDispatcher) {
        settingsRepository.saveBaseLayer(layer)
    }

    fun selectHikingLayer() {
        hikingLayer.update { layerSpec ->
            if (layerSpec == null) {
                getHikingLayerSpec(hikingLayerZoomRanges)
            } else {
                null
            }
        }
    }

    fun selectGpxLayer() {
        _gpxDetailsUiModel.update { model ->
            model?.copy(isVisible = model.isVisible.not())
        }
    }

    fun updateGpxSlopeColors(useSlopeColors: Boolean) {
        _gpxDetailsUiModel.update { uiModel ->
            uiModel?.copy(useSlopeColors = useSlopeColors)
        }
    }

    fun reverseGpx() {
        _gpxDetailsUiModel.update { uiModel ->
            uiModel?.copy(
                id = UUID.randomUUID().toString(),
                geoPoints = uiModel.geoPoints.reversed(),
                arrowGeoPoints = calculateDirectionArrows(uiModel.geoPoints.reversed()),
                waypoints = uiModel.waypoints
                    .map { waypoint ->
                        waypoint.copy(
                            waypointType = when (waypoint.waypointType) {
                                WaypointType.START -> WaypointType.END
                                WaypointType.END -> WaypointType.START
                                WaypointType.INTERMEDIATE -> WaypointType.INTERMEDIATE
                                WaypointType.ROUND_TRIP -> WaypointType.ROUND_TRIP
                            }
                        )
                    }
            )
        }
    }

    fun clearGpxDetails() {
        _gpxDetailsUiModel.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun getHikingLayerSpec(zoomRanges: List<TileZoomRange>): HikingLayer {
        return HikingLayer(LayerType.HUNGARIAN_HIKING_LAYER, AwsHikingTileSource(hikingTileUrlProvider, zoomRanges))
    }

    private suspend fun showError(throwable: Throwable) {
        Timber.e(throwable)

        if (throwable is DomainException) {
            _errorMessage.emit(throwable.messageRes)
        } else {
            exceptionLogger.recordException(throwable)
        }
    }

    private fun restoreSavedState() {
        if (savedFileUri != null) {
            loadGpx(savedFileUri.toUri())
        }
    }

}
