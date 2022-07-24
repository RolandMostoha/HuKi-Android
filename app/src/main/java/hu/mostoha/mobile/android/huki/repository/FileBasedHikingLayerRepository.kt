package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.readRawJson
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import javax.inject.Inject

class FileBasedHikingLayerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : HikingLayerRepository {

    override suspend fun getHikingLayerZoomRanges(): List<TileZoomRange> {
        return context.resources.readRawJson(R.raw.hiking_layer_tile_zoom_ranges)
    }

}
