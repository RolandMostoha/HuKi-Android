package hu.mostoha.mobile.android.turistautak.configuration

import android.content.Context
import org.osmdroid.tileprovider.util.StorageUtils
import timber.log.Timber
import java.io.File

object OsmConfiguration {

    const val URL_HIKING_LAYER_FILE = "https://data2.openstreetmap.hu/tt.mbtiles"

    // First 1000 row of tt.mbtiles ~ 3 MB
    // const val URL_HIKING_LAYER_FILE = "https://drive.google.com/file/d/17JRuL5ambcl3qeJ7XV8A4BpCVdHsAlMJ/view?usp=sharing"

    private const val FILE_NAME_HIKING_LAYER = "TuraReteg.mbtiles"
    private const val DIRECTORY_NAME_OSMDROID = "osmdroid"
    private const val DIRECTORY_NAME_TILES_ARCHIVE = "tiles"
    private const val DIRECTORY_NAME_LAYERS = "layers"

    private val osmDroidBasePath: String? = null
    private val osmDroidCachePath: String? = null
    private val osmDroidLayerPath: String? = null

    fun getOsmDroidBaseDirectory(context: Context): File {
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
            File(osmDroidBasePath)
        }
    }

    fun getOsmDroidCacheDirectory(context: Context): File {
        return if (osmDroidCachePath == null) {
            val file = File(getOsmDroidBaseDirectory(context), DIRECTORY_NAME_TILES_ARCHIVE)
            if (!file.exists()) {
                val success = file.mkdirs()
                Timber.d("Created cache storage ${file.path} : $success")
            }

            Timber.d("Using cache storage ${file.path}")
            file
        } else {
            File(osmDroidCachePath)
        }
    }

    private fun getOsmDroidLayerDirectory(context: Context): File {
        return if (osmDroidLayerPath == null) {
            val file = File(getOsmDroidBaseDirectory(context), DIRECTORY_NAME_LAYERS)
            if (!file.exists()) {
                val success = file.mkdirs()
                Timber.d("Created layers storage ${file.path} : $success")
            }

            Timber.d("Using layers storage storage ${file.path}")
            file
        } else {
            File(osmDroidLayerPath)
        }
    }

    fun getHikingLayerFile(context: Context): File {
        return File(getOsmDroidLayerDirectory(context), FILE_NAME_HIKING_LAYER)
    }

}
