package hu.mostoha.mobile.android.turistautak.configuration

import android.content.Context
import org.osmdroid.tileprovider.util.StorageUtils
import timber.log.Timber
import java.io.File

object OsmConfiguration {

    const val FILE_PATH_HIKING_LAYER = "/layers/TuraReteg.mbtiles"

    private const val DIRECTORY_NAME_OSMDROID = "osmdroid"
    private const val DIRECTORY_NAME_TILES_ARCHIVE = "tiles"

    private val osmDroidBasePath: File? = null
    private val osmDroidCachePath: File? = null

    fun getOsmDroidBasePath(context: Context): File {
        return if (osmDroidBasePath == null) {
            val storage = StorageUtils.getStorage(context)
            val path = storage.path
            Timber.d("Trying to use storage: $path")

            val file = File(path, DIRECTORY_NAME_OSMDROID)
            if (!file.exists()) {
                val success = file.mkdirs()
                Timber.d("Created storage ${file.path} : $success")
            }

            Timber.d("Using storage ${file.path}")
            file
        } else {
            osmDroidBasePath
        }
    }

    fun getOsmDroidCachePath(context: Context): File {
        return if (osmDroidCachePath == null) {
            val file = File(getOsmDroidBasePath(context), DIRECTORY_NAME_TILES_ARCHIVE)
            if (!file.exists()) {
                val success = file.mkdirs()
                Timber.d("Created cache storage ${file.path} : $success")
            }

            Timber.d("Using cache storage ${file.path}")
            file
        } else {
            osmDroidCachePath
        }
    }

}
