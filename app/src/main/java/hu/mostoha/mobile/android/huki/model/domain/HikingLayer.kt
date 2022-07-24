package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.tileprovider.tilesource.ITileSource

data class HikingLayer(
    override val layerType: LayerType,
    override val tileSource: ITileSource
) : LayerSpec(layerType, tileSource)