package hu.mostoha.mobile.android.turistautak.osmdroid

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.turistautak.BuildConfig
import hu.mostoha.mobile.android.turistautak.extensions.getOrCreateDirectory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.util.StorageUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class OsmConfiguration @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val URL_HIKING_LAYER_FILE = "https://data2.openstreetmap.hu/tt.mbtiles"

        private const val DIRECTORY_NAME_OSM_DROID = "osmdroid"
        private const val DIRECTORY_NAME_CACHE = "tiles"
        private const val DIRECTORY_NAME_LAYERS = "layers"
        private const val FILE_NAME_HIKING_LAYER = "TuraReteg.mbtiles"

        private const val KEY_GLOBAL_SHARED_PREFERENCES = "KEY_GLOBAL_SHARED_PREFERENCES"
    }

    private val osmDroidBasePath: String? = null
    private val osmDroidCachePath: String? = null
    private val osmDroidLayerPath: String? = null

    fun getHikingLayerFileUrl() = URL_HIKING_LAYER_FILE

    fun init() {
        Configuration.getInstance().apply {
            if (BuildConfig.DEBUG) {
                isDebugMapView = true
                isDebugMode = true
                isDebugTileProviders = true
                isDebugMapTileDownloader = true
            }

            osmdroidBasePath = getOsmDroidBaseDirectory()
            osmdroidTileCache = getOsmDroidCacheDirectory()

            load(context, context.getSharedPreferences(KEY_GLOBAL_SHARED_PREFERENCES, Context.MODE_PRIVATE))
        }
    }

    private fun getOsmDroidBaseDirectory(): File {
        return if (osmDroidBasePath == null) {
            val storage = StorageUtils.getStorage(context)
            val path = storage.path

            val file = getOrCreateDirectory(path, DIRECTORY_NAME_OSM_DROID)
                ?: dirCreationError("$path/$DIRECTORY_NAME_OSM_DROID")
            logDirCreated("Base dir: ${file.path}")

            file
        } else {
            File(osmDroidBasePath)
        }
    }

    private fun getOsmDroidCacheDirectory(): File {
        return if (osmDroidCachePath == null) {
            val basePath = getOsmDroidBaseDirectory().path
            val file = getOrCreateDirectory(basePath, DIRECTORY_NAME_CACHE)
                ?: dirCreationError("$basePath/$DIRECTORY_NAME_CACHE")
            logDirCreated("Cache dir: ${file.path}")

            file
        } else {
            File(osmDroidCachePath)
        }
    }

    fun getHikingLayerFile(): File {
        return File(getOsmDroidLayerDirectory(), FILE_NAME_HIKING_LAYER)
    }

    private fun getOsmDroidLayerDirectory(): File {
        return if (osmDroidLayerPath == null) {
            val basePath = getOsmDroidBaseDirectory().path
            val file = getOrCreateDirectory(basePath, DIRECTORY_NAME_LAYERS)
                ?: dirCreationError("$basePath/$DIRECTORY_NAME_LAYERS")
            logDirCreated("Layer dir: ${file.path}")

            file
        } else {
            File(osmDroidLayerPath)
        }
    }

    private fun dirCreationError(path: String): Nothing = error("OSM directory creation error: $path")

    private fun logDirCreated(pathText: String) = Timber.i("Using OSM $pathText")

}
