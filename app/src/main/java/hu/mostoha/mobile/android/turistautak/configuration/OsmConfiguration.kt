package hu.mostoha.mobile.android.turistautak.configuration

import android.content.Context
import android.util.Log
import hu.mostoha.mobile.android.turistautak.extensions.TAG
import org.osmdroid.tileprovider.util.StorageUtils
import java.io.File

object OsmConfiguration {

    private const val FILE_PATH_HIKING_LAYER = "/layers/TuraReteg.mbtiles"
    private const val DIRECTORY_NAME_OSMDROID = "osmdroid"
    private const val DIRECTORY_NAME_TILES_ARCHIVE = "tiles"

    private val osmDroidBasePath: File? = null
    private val osmDroidCachePath: File? = null

    fun getOsmDroidBasePath(context: Context): File {
        return if (osmDroidBasePath == null) {
            val storage = StorageUtils.getStorage(context)
            val path = storage.path
            Log.d(this.TAG, "Trying to use storage: $path")

            val file = File(path, DIRECTORY_NAME_OSMDROID)
            if (!file.exists()) {
                val success = file.mkdirs()
                Log.d(this.TAG, "Created storage ${file.path} : $success")
            }

            Log.d(this.TAG, "Using storage ${file.path}")
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
                Log.d(this.TAG, "Created cache storage ${file.path} : $success")
            }

            Log.d(this.TAG, "Using cache storage ${file.path}")
            file
        } else {
            osmDroidCachePath
        }
    }

    fun getHikingLayerFile(context: Context): File? {
        val file = File(getOsmDroidBasePath(context).path + FILE_PATH_HIKING_LAYER)
        if (file.exists()) {
            return file
        }
        return null
    }

    fun isHikingLayerFileExist(context: Context): Boolean {
        val file = File(getOsmDroidBasePath(context).path + FILE_PATH_HIKING_LAYER)
        return file.exists()
    }

}
