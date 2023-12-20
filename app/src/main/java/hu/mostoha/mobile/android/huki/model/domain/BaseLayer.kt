package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.osmdroid.tilesource.MapnikTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.TuHuTileSource
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

sealed class BaseLayer(layerType: LayerType, tileSource: ITileSource) : LayerSpec(layerType, tileSource) {

    object Mapnik : BaseLayer(LayerType.MAPNIK, MapnikTileSource)

    object OpenTopo : BaseLayer(LayerType.OPEN_TOPO, TileSourceFactory.OpenTopo)

    object TuHu : BaseLayer(LayerType.TUHU, TuHuTileSource)

}
