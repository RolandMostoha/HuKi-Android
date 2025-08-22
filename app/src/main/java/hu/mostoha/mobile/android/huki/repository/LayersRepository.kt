package hu.mostoha.mobile.android.huki.repository

import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange

interface LayersRepository {

    suspend fun getHikingLayerZoomRanges(): List<TileZoomRange>

}
