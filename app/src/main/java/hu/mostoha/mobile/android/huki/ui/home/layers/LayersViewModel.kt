package hu.mostoha.mobile.android.huki.ui.home.layers

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.interactor.LayersInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
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
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LayersViewModel @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val exceptionLogger: ExceptionLogger,
    private val hikingTileUrlProvider: HikingTileUrlProvider,
    private val layersInteractor: LayersInteractor,
    private val layersUiModelMapper: LayersUiModelMapper,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private lateinit var hikingLayerZoomRanges: List<TileZoomRange>

    private val baseLayer = MutableStateFlow<BaseLayer>(BaseLayer.Mapnik)

    private val hikingLayer = MutableStateFlow<HikingLayer?>(null)

    val layersConfig = baseLayer.combine(hikingLayer) { baseLayer, hikingLayer ->
        LayersConfig(baseLayer, hikingLayer)
    }.stateIn(viewModelScope, WhileViewSubscribed, LayersConfig(baseLayer = BaseLayer.Mapnik, hikingLayer = null))

    private val _gpxDetailsUiModel = MutableStateFlow<GpxDetailsUiModel?>(null)
    val gpxDetailsUiModel: StateFlow<GpxDetailsUiModel?> = _gpxDetailsUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    val layerAdapterItems = layersConfig.combine(gpxDetailsUiModel) { layersConfig, gpxDetails ->
        layersUiModelMapper.mapLayerAdapterItems(layersConfig.baseLayer, layersConfig.hikingLayer, gpxDetails)
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    private val _errorMessage = MutableStateFlow<Message.Res?>(null)
    val errorMessage: SharedFlow<Message.Res?> = _errorMessage
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    init {
        viewModelScope.launch(dispatcher) {
            layersInteractor.requestHikingLayerZoomRanges()
                .onEach { zoomRanges ->
                    hikingLayerZoomRanges = zoomRanges

                    hikingLayer.emit(getHikingLayerSpec(zoomRanges))
                }
                .catch { showError(it) }
                .collect()
        }
    }

    fun selectLayer(layerType: LayerType) = viewModelScope.launch(dispatcher) {
        when (layerType) {
            LayerType.MAPNIK -> baseLayer.emit(BaseLayer.Mapnik)
            LayerType.OPEN_TOPO -> baseLayer.emit(BaseLayer.OpenTopo)
            LayerType.HUNGARIAN_HIKING_LAYER -> switchHikingLayerVisibility()
            LayerType.GPX -> switchGpxDetailsVisibility()
        }
    }

    suspend fun loadGpx(uri: Uri?) {
        layersInteractor.requestGpxDetails(uri)
            .onEach {
                val gpxDetails = layersUiModelMapper.mapGpxDetails(it)

                analyticsService.gpxImported(gpxDetails.name)

                _gpxDetailsUiModel.emit(gpxDetails)
            }
            .catch { showError(it) }
            .collect()
    }

    suspend fun loadRoutePlannerGpx(uri: Uri) {
        layersInteractor.requestRoutePlannerGpxDetails(uri)
            .onEach { _gpxDetailsUiModel.emit(layersUiModelMapper.mapGpxDetails(it)) }
            .catch { showError(it) }
            .collect()
    }

    fun clearGpxDetails() {
        _gpxDetailsUiModel.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun switchHikingLayerVisibility() {
        hikingLayer.update { layerSpec ->
            if (layerSpec == null) {
                getHikingLayerSpec(hikingLayerZoomRanges)
            } else {
                null
            }
        }
    }

    private fun switchGpxDetailsVisibility() {
        _gpxDetailsUiModel.update { model ->
            model?.copy(isVisible = model.isVisible.not())
        }
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

}
