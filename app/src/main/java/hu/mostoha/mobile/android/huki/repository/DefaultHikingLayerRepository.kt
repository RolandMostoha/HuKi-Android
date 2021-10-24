package hu.mostoha.mobile.android.huki.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.requireSystemService
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject


class DefaultHikingLayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val osmConfiguration: OsmConfiguration
) : HikingLayerRepository {

    override suspend fun getHikingLayerFile(): File? {
        val file = osmConfiguration.getHikingLayerFile()
        return if (file.exists()) file else null
    }

    override suspend fun downloadHikingLayerFile(): Long {
        val request = DownloadManager.Request(Uri.parse(osmConfiguration.getHikingLayerFileUrl()))
            .setTitle(context.getString(R.string.download_layer_notification_title))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = context.requireSystemService<DownloadManager>()
        val requestId = downloadManager.enqueue(request)

        Timber.i("Download requested with requestId: $requestId")

        return requestId
    }

    override suspend fun saveHikingLayerFile(downloadId: Long) {
        val downloadManager = context.requireSystemService<DownloadManager>()
        val downloadManagerUri = downloadManager.getUriForDownloadedFile(downloadId)
        if (downloadManagerUri == null) {
            Timber.e("Downloaded layer URI is missing: $downloadId")
            throw FileNotFoundException()
        }

        val inputStream = context.contentResolver.openInputStream(downloadManagerUri)
        if (inputStream == null) {
            Timber.e("Downloaded layer input stream is null for URI: $downloadManagerUri")
            throw FileNotFoundException()
        }

        val destinationFile = osmConfiguration.getHikingLayerFile()
        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output, DEFAULT_BUFFER_SIZE)
            }
        }
    }

}
