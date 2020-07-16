package hu.mostoha.mobile.android.turistautak.configuration

import android.content.Context
import hu.mostoha.mobile.android.turistautak.extensions.getOrCreateDirectory
import org.osmdroid.tileprovider.util.StorageUtils
import java.io.File

object OsmConfiguration {

    private const val DIRECTORY_NAME_OSM_DROID = "osmdroid"
    private const val DIRECTORY_NAME_CACHE = "tiles"
    private const val DIRECTORY_NAME_LAYERS = "layers"

    private val osmDroidBasePath: String? = null
    private val osmDroidCachePath: String? = null
    private val osmDroidLayerPath: String? = null

    fun getOsmDroidBaseDirectory(context: Context): File {
        return if (osmDroidBasePath == null) {
            val storage = StorageUtils.getStorage(context)
            val path = storage.path

            getOrCreateDirectory(
                path,
                DIRECTORY_NAME_OSM_DROID
            ) ?: error("Could not get or create OSM layer directory for: $DIRECTORY_NAME_CACHE")
        } else {
            File(osmDroidBasePath)
        }
    }

    fun getOsmDroidCacheDirectory(context: Context): File {
        return if (osmDroidCachePath == null) {
            getOrCreateDirectory(
                getOsmDroidBaseDirectory(context).path,
                DIRECTORY_NAME_CACHE
            ) ?: error("Could not get or create OSM layer directory for: $DIRECTORY_NAME_CACHE")
        } else {
            File(osmDroidCachePath)
        }
    }

    fun getOsmDroidLayerDirectory(context: Context): File {
        return if (osmDroidLayerPath == null) {
            getOrCreateDirectory(
                getOsmDroidBaseDirectory(context).path,
                DIRECTORY_NAME_LAYERS
            ) ?: error("Could not get or create OSM layer directory for: $DIRECTORY_NAME_LAYERS")
        } else {
            File(osmDroidLayerPath)
        }
    }

}
