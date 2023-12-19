package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.extensions.getFileName
import hu.mostoha.mobile.android.huki.extensions.readRawJson
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.interactor.exception.GpxUriNullException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

class FileBasedLayersRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val layersDomainModelMapper: LayersDomainModelMapper,
    private val gpxConfiguration: GpxConfiguration,
    private val exceptionLogger: ExceptionLogger,
) : LayersRepository {

    override suspend fun getHikingLayerZoomRanges(): List<TileZoomRange> {
        return withContext(ioDispatcher) {
            context.resources.readRawJson(R.raw.hiking_layer_tile_zoom_ranges)
        }
    }

    override suspend fun getGpxDetails(fileUri: Uri?): GpxDetails {
        return withContext(ioDispatcher) {
            Timber.d("Importing GPX by URI: $fileUri")

            if (fileUri == null) {
                throw GpxUriNullException(IllegalArgumentException("Uri is null"))
            }

            val fileName = "${fileUri.getFileName(context)}.gpx"

            var inputStream = context.contentResolver.openInputStream(fileUri)!!

            val externalFilePath = Paths.get(gpxConfiguration.getExternalGpxDirectory() + "/$fileName")

            // Copy file to internal storage if not exists (first time import)
            if (!Files.exists(externalFilePath)) {
                try {
                    inputStream = externalFilePath.toFile().apply {
                        copyFrom(inputStream)
                    }.inputStream()
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }

            val gpx = GPXParser().parse(inputStream)

            layersDomainModelMapper.mapGpxDetails(externalFilePath.toUri().toString(), fileName, gpx)
        }
    }

    override suspend fun getRoutePlannerGpxDetails(fileUri: Uri): GpxDetails {
        return withContext(ioDispatcher) {
            Timber.d("Importing route planner GPX by URI: $fileUri")

            val fileName = "${fileUri.getFileName(context)}.gpx"

            val inputStream = context.contentResolver.openInputStream(fileUri)!!

            val gpx = GPXParser().parse(inputStream)

            layersDomainModelMapper.mapGpxDetails(fileUri.toString(), fileName, gpx)
        }
    }

    override suspend fun getGpxHistory(): GpxHistory {
        return withContext(ioDispatcher) {
            val routePlannerGpxDirectory = File(gpxConfiguration.getRoutePlannerGpxDirectory())

            var routePlannerGpxDirectoryFiles = routePlannerGpxDirectory.listFiles()
            if (routePlannerGpxDirectoryFiles == null) {
                val exception =
                    IllegalStateException("Route planner GPX directory doesn't exist while getting GPX history")

                Timber.e(exception)

                exceptionLogger.recordException(exception)

                routePlannerGpxDirectoryFiles = emptyArray()
            }
            val routePlannerGpxHistoryItems = routePlannerGpxDirectoryFiles.map { file ->
                getGpxHistoryItem(file)
            }

            val externalGpxDirectory = File(gpxConfiguration.getExternalGpxDirectory())
            var externalGpxGpxDirectoryFiles = externalGpxDirectory.listFiles()
            if (externalGpxGpxDirectoryFiles == null) {
                val exception = IllegalStateException("External GPX directory doesn't exist while getting GPX history")

                Timber.e(exception)

                exceptionLogger.recordException(exception)

                externalGpxGpxDirectoryFiles = emptyArray()
            }
            val externalGpxGpxHistoryItems = externalGpxGpxDirectoryFiles.map { file ->
                getGpxHistoryItem(file)
            }

            GpxHistory(routePlannerGpxHistoryItems, externalGpxGpxHistoryItems)
        }
    }

    override suspend fun deleteGpx(fileUri: Uri) {
        withContext(ioDispatcher) {
            fileUri.toFile().delete()
        }
    }

    override suspend fun renameGpx(fileUri: Uri, newName: String) {
        withContext(ioDispatcher) {
            val sourceFile = fileUri.toFile()
            val targetFile = File(sourceFile.parent, "$newName.gpx")

            if (!targetFile.exists()) {
                targetFile.createNewFile()
            }

            val isSuccessful = sourceFile.renameTo(targetFile)

            if (!isSuccessful) {
                error("Renaming GPX file was unsuccessful. source: $sourceFile, target: $targetFile")
            }
        }
    }

    private fun getGpxHistoryItem(file: File): GpxHistoryItem {
        val fileUri = file.toUri()
        val fileName = "${fileUri.getFileName(context)}.gpx"
        val lastModified = file.lastModified().toLocalDateTime()

        val inputStream = context.contentResolver.openInputStream(fileUri)!!

        val gpx = GPXParser().parse(inputStream)

        return layersDomainModelMapper.mapGpxHistoryItem(fileUri, fileName, gpx, lastModified)
    }

}
