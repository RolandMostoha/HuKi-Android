package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.tileprovider.tilesource.ITileSource

open class LayerSpec(open val layerType: LayerType, open val tileSource: ITileSource)
