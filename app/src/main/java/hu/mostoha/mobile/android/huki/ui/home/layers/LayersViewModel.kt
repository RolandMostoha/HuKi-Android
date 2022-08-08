package hu.mostoha.mobile.android.huki.ui.home.layers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.interactor.HikingLayerInteractor
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.LayersConfig
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LayersViewModel @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val hikingTileUrlProvider: HikingTileUrlProvider,
    hikingLayerInteractor: HikingLayerInteractor
) : ViewModel() {

    private lateinit var hikingLayerZoomRanges: List<TileZoomRange>

    private val baseLayer = MutableStateFlow<BaseLayer>(BaseLayer.Mapnik)

    private val hikingLayer = MutableStateFlow<HikingLayer?>(null)

    val layersConfig = baseLayer.combine(hikingLayer) { baseLayer, hikingLayer ->
        LayersConfig(baseLayer, hikingLayer)
    }.stateIn(viewModelScope, WhileViewSubscribed, LayersConfig(baseLayer = BaseLayer.Mapnik, hikingLayer = null))

    val layerAdapterItems = layersConfig.map { (baseLayer, hikingLayer) ->
        listOf(
            LayersAdapterItem.Header(R.string.layers_base_layers_header),
            LayersAdapterItem.Layer(
                layerType = LayerType.MAPNIK,
                titleRes = R.string.layers_mapnik_title,
                drawableRes = R.drawable.ic_layers_mapnik,
                isSelected = baseLayer.layerType == LayerType.MAPNIK
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.OPEN_TOPO,
                titleRes = R.string.layers_open_topo_title,
                drawableRes = R.drawable.ic_layers_open_topo,
                isSelected = baseLayer.layerType == LayerType.OPEN_TOPO
            ),
            LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
            LayersAdapterItem.Layer(
                layerType = LayerType.ONLINE_HIKING_LAYER,
                titleRes = R.string.layers_hiking_hungarian_title,
                drawableRes = R.drawable.ic_layers_hiking,
                isSelected = hikingLayer?.layerType == LayerType.ONLINE_HIKING_LAYER
            )
        )
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    init {
        viewModelScope.launch(dispatcher) {
            hikingLayerInteractor.requestHikingLayerZoomRanges()
                .onEach { zoomRanges ->
                    hikingLayerZoomRanges = zoomRanges

                    hikingLayer.emit(getHikingLayerSpec(zoomRanges))
                }
                .catch { Timber.e(it) }
                .collect()
        }
    }

    fun selectLayer(layerType: LayerType) = viewModelScope.launch(dispatcher) {
        when (layerType) {
            LayerType.MAPNIK -> baseLayer.emit(BaseLayer.Mapnik)
            LayerType.OPEN_TOPO -> baseLayer.emit(BaseLayer.OpenTopo)
            LayerType.ONLINE_HIKING_LAYER -> {
                hikingLayer.update { layerSpec ->
                    if (layerSpec == null) {
                        getHikingLayerSpec(hikingLayerZoomRanges)
                    } else {
                        null
                    }
                }
            }
        }
    }

    private fun getHikingLayerSpec(zoomRanges: List<TileZoomRange>): HikingLayer {
        return HikingLayer(LayerType.ONLINE_HIKING_LAYER, AwsHikingTileSource(hikingTileUrlProvider, zoomRanges))
    }

}
