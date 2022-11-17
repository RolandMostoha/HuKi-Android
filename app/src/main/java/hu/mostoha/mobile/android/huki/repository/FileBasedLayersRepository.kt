package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.getFileName
import hu.mostoha.mobile.android.huki.extensions.readRawJson
import hu.mostoha.mobile.android.huki.interactor.exception.GpxUriNullException
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import io.ticofab.androidgpxparser.parser.GPXParser
import timber.log.Timber
import javax.inject.Inject

class FileBasedLayersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val layersDomainModelMapper: LayersDomainModelMapper
) : LayersRepository {

    override suspend fun getHikingLayerZoomRanges(): List<TileZoomRange> {
        return context.resources.readRawJson(R.raw.hiking_layer_tile_zoom_ranges)
    }

    override suspend fun getGpxDetails(fileUri: Uri?): GpxDetails {
        Timber.d("Importing GPX by URI: $fileUri")

        if (fileUri == null) {
            throw GpxUriNullException(IllegalArgumentException("Uri is null"))
        }

        val inputStream = context.contentResolver.openInputStream(fileUri)

        val fileName = "${fileUri.getFileName(context)}.gpx"

        val gpx = GPXParser().parse(inputStream)

        return layersDomainModelMapper.mapGpxDetails(fileName, gpx)
    }

}
