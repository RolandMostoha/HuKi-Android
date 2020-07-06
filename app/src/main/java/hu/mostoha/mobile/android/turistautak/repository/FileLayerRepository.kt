package hu.mostoha.mobile.android.turistautak.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.extensions.requireSystemService
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject


class FileLayerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LayerRepository {

    override fun getHikingLayerFile(): File? {
        val file = OsmConfiguration.getHikingLayerFile(context)
        if (file.exists()) {
            return file
        }
        return null
    }

    override fun downloadHikingLayerFile(): Long {
        val request = DownloadManager.Request(Uri.parse(OsmConfiguration.URL_HIKING_LAYER_FILE))
            .setTitle(context.getString(R.string.download_layer_notification_title))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager = context.requireSystemService<DownloadManager>()
        val requestId = downloadManager.enqueue(request)

        Timber.i("Download requested requestId: $requestId")

        return requestId
    }

    override fun saveHikingLayerFile(downloadId: Long) {
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

        val destinationFile = OsmConfiguration.getHikingLayerFile(context)
        inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output, DEFAULT_BUFFER_SIZE)
            }
        }
    }

}