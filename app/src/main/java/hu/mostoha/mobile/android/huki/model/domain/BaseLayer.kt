package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

sealed class BaseLayer(layerType: LayerType, tileSource: ITileSource) : LayerSpec(layerType, tileSource) {

    object Mapnik : BaseLayer(LayerType.MAPNIK, TileSourceFactory.MAPNIK)

    object OpenTopo : BaseLayer(LayerType.OPEN_TOPO, TileSourceFactory.OpenTopo)

}