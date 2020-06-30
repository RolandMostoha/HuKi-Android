package hu.mostoha.mobile.android.turistautak.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class FileLayerRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : LayerRepository {

    override fun getHikingLayerFile(): File? {
        val file = OsmConfiguration.getOsmDroidLayerPath(context)
        if (file.exists()) {
            return file
        }
        return null
    }

    override fun downloadHikingLayerFile(): Long {
        val request = DownloadManager.Request(Uri.parse(OsmConfiguration.URL_HIKING_LAYER_FILE))
            .setTitle(context.getString(R.string.download_layer_notification_title))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                OsmConfiguration.getOsmDroidLayerPath(context).path
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService<DownloadManager>()!!
        val requestId = downloadManager.enqueue(request)

        Timber.d("Download requested with requestId: $requestId")

        return requestId
    }

}