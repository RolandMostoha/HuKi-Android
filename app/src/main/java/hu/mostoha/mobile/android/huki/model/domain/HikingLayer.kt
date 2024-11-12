package hu.mostoha.mobile.android.huki.model.domain

import org.osmdroid.tileprovider.tilesource.ITileSource

data class HikingLayer(
    val layerType: LayerType,
    val tileSource: ITileSource
)
