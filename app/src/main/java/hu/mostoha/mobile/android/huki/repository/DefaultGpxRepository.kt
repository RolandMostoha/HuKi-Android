package hu.mostoha.mobile.android.huki.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.database.GpxHistoryDao
import hu.mostoha.mobile.android.huki.di.module.DbDispatcher
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.extensions.getFileName
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.interactor.exception.GpxUriNullException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.db.GpxHistoryEntity
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.mapper.LayersDomainModelMapper
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import io.ticofab.androidgpxparser.parser.GPXParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParserException
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

class DefaultGpxRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DbDispatcher private val dbDispatcher: CoroutineDispatcher,
    private val layersDomainModelMapper: LayersDomainModelMapper,
    private val gpxConfiguration: GpxConfiguration,
    private val gpxHistoryDao: GpxHistoryDao,
    private val dateTimeProvider: DateTimeProvider,
    private val exceptionLogger: ExceptionLogger,
) : GpxRepository {

    companion object {
        private const val MAX_RECENT_HISTORY_ITEMS = 4
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
            val externalFile = externalFilePath.toFile()

            // Copy file to internal storage if not exists (first time import)
            if (!Files.exists(externalFilePath)) {
                try {
                    inputStream = externalFile
                        .apply { copyFrom(inputStream) }
                        .inputStream()
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }

            val gpx = GPXParser().parse(inputStream)

            withContext(dbDispatcher) {
                gpxHistoryDao.insertAll(
                    GpxHistoryEntity(
                        filePath = externalFile.toUri().toString(),
                        lastOpened = dateTimeProvider.nowInMillis(),
                        type = GpxType.EXTERNAL
                    )
                )
            }

            layersDomainModelMapper.mapGpxDetails(externalFilePath.toUri().toString(), fileName, gpx)
        }
    }

    override suspend fun getRoutePlannerGpxDetails(fileUri: Uri): GpxDetails {
        return withContext(ioDispatcher) {
            Timber.d("Importing route planner GPX by URI: $fileUri")

            val fileName = "${fileUri.getFileName(context)}.gpx"
            val inputStream = context.contentResolver.openInputStream(fileUri)!!
            val gpx = GPXParser().parse(inputStream)

            withContext(dbDispatcher) {
                gpxHistoryDao.insertAll(
                    GpxHistoryEntity(
                        filePath = fileUri.toString(),
                        lastOpened = dateTimeProvider.nowInMillis(),
                        type = GpxType.ROUTE_PLANNER
                    )
                )
            }

            layersDomainModelMapper.mapGpxDetails(fileUri.toString(), fileName, gpx)
        }
    }

    override suspend fun getGpxHistory(): GpxHistory {
        return withContext(ioDispatcher) {
            val routePlannerGpxDirectory = File(gpxConfiguration.getRoutePlannerGpxDirectory())

            var routePlannerGpxDirectoryFiles = routePlannerGpxDirectory.listFiles()
            if (routePlannerGpxDirectoryFiles == null) {
                val exception = IllegalStateException("Route planner GPX directory doesn't exist while get GPX history")

                Timber.e(exception)

                exceptionLogger.recordException(exception)

                routePlannerGpxDirectoryFiles = emptyArray()
            }
            val routePlannerGpxHistoryItems = routePlannerGpxDirectoryFiles
                .mapNotNull { file ->
                    getGpxHistoryItem(file, GpxType.ROUTE_PLANNER)
                }
                .sortedByDescending { it.lastModified }

            val externalGpxDirectory = File(gpxConfiguration.getExternalGpxDirectory())
            var externalGpxGpxDirectoryFiles = externalGpxDirectory.listFiles()
            if (externalGpxGpxDirectoryFiles == null) {
                val exception = IllegalStateException("External GPX directory doesn't exist while get GPX history")

                Timber.e(exception)

                exceptionLogger.recordException(exception)

                externalGpxGpxDirectoryFiles = emptyArray()
            }
            val externalGpxGpxHistoryItems = externalGpxGpxDirectoryFiles
                .mapNotNull { file ->
                    getGpxHistoryItem(file, GpxType.EXTERNAL)
                }
                .sortedByDescending { it.lastModified }

            GpxHistory(routePlannerGpxHistoryItems, externalGpxGpxHistoryItems)
        }
    }

    override suspend fun getRecentGpxHistory(): List<GpxHistoryItem> {
        return withContext(ioDispatcher) {
            val allGpxHistory = getGpxHistory()
            val allGpxHistoryItems = (allGpxHistory.routePlannerGpxList + allGpxHistory.externalGpxList).toMutableList()
            val allGpxFilePaths = allGpxHistoryItems.map { it.fileUri.toString() }.toSet()
            val recentGpxHistory = gpxHistoryDao.getEntities().first()
            val result = mutableListOf<GpxHistoryItem>()

            // Delete outdated GPX history items from DB
            recentGpxHistory.onEach { recentItem ->
                if (recentItem.filePath !in allGpxFilePaths) {
                    withContext(dbDispatcher) {
                        gpxHistoryDao.delete(recentItem.filePath)
                        allGpxHistoryItems.removeIf { it.fileUri.toString() == recentItem.filePath }
                    }
                } else {
                    result.add(allGpxHistoryItems.first { it.fileUri.toString() == recentItem.filePath })
                }
            }

            result
                .plus(allGpxHistoryItems.filterNot { it in result })
                .take(MAX_RECENT_HISTORY_ITEMS)
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
                Timber.e("Renaming GPX file was unsuccessful. source: $sourceFile, target: $targetFile")
            }
        }
    }

    private fun getGpxHistoryItem(file: File, type: GpxType): GpxHistoryItem? {
        val fileUri = file.toUri()
        val fileName = "${fileUri.getFileName(context)}.gpx"
        val lastModifiedDateTime = file.lastModified().toLocalDateTime()

        val inputStream = context.contentResolver.openInputStream(fileUri)!!

        val gpx = try {
            GPXParser().parse(inputStream)
        } catch (ioException: IOException) {
            Timber.w(ioException, "IOException while parsing GPX History item")

            return null
        } catch (xmlPullParserException: XmlPullParserException) {
            Timber.w(xmlPullParserException, "XmlPullParserException while parsing GPX History item")

            return null
        }

        return layersDomainModelMapper.mapGpxHistoryItem(fileUri, fileName, type, gpx, lastModifiedDateTime)
    }

}
