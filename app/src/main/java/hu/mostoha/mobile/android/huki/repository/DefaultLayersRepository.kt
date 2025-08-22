package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.readRawJson
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultLayersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LayersRepository {

    override suspend fun getHikingLayerZoomRanges(): List<TileZoomRange> {
        return withContext(ioDispatcher) {
            context.resources.readRawJson(R.raw.hiking_layer_tile_zoom_ranges)
        }
    }

}
