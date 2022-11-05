package hu.mostoha.mobile.android.huki.osmdroid

import android.content.Context
import android.webkit.WebSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.extensions.getOrCreateDirectory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.ONE_WEEK
import org.osmdroid.tileprovider.util.StorageUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OsmConfiguration @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        /**
         *  Default cache size increased from 600MB to 800MB
         */
        private const val TILE_FILE_SYSTEM_CACHE_MAX_BYTES = 800L * 1024 * 1024
        private const val TILE_FILE_SYSTEM_CACHE_TARGET_BYTES = 700L * 1024 * 1024

        /**
         *  Default tile count cache is doubled - the app uses 2 tile layers by default
         */
        private const val DEFAULT_TILE_COUNT_IN_CACHE = (2 * 9).toShort()

        private const val DIRECTORY_NAME_OSM_DROID = "osmdroid"
        private const val DIRECTORY_NAME_CACHE = "tiles"

        private const val KEY_GLOBAL_SHARED_PREFERENCES = "KEY_GLOBAL_SHARED_PREFERENCES"
    }

    private val osmDroidBasePath: String? = null
    private val osmDroidCachePath: String? = null

    private val isDebug = false

    fun init() {
        Configuration.getInstance().apply {
            isDebugMapView = isDebug
            isDebugMode = isDebug
            isDebugTileProviders = isDebug
            isDebugMapTileDownloader = isDebug

            osmdroidBasePath = getOsmDroidBaseDirectory()
            osmdroidTileCache = getOsmDroidCacheDirectory()
            userAgentValue = "${WebSettings.getDefaultUserAgent(context)} ${BuildConfig.APPLICATION_ID}"
            expirationExtendedDuration = ONE_WEEK
            tileFileSystemCacheMaxBytes = TILE_FILE_SYSTEM_CACHE_MAX_BYTES
            tileFileSystemCacheTrimBytes = TILE_FILE_SYSTEM_CACHE_TARGET_BYTES
            cacheMapTileCount = DEFAULT_TILE_COUNT_IN_CACHE

            load(context, context.getSharedPreferences(KEY_GLOBAL_SHARED_PREFERENCES, Context.MODE_PRIVATE))
        }
    }

    private fun getOsmDroidBaseDirectory(): File {
        val baseDirectory = if (osmDroidBasePath != null) {
            File(osmDroidBasePath)
        } else {
            val storage = StorageUtils.getBestWritableStorage(context)
            val path = storage.path

            val file = getOrCreateDirectory(
                parent = path,
                child = DIRECTORY_NAME_OSM_DROID
            ) ?: error("OSM directory creation error: $path/$DIRECTORY_NAME_OSM_DROID")

            file
        }

        Timber.i("Using OSM Base DIR: ${baseDirectory.path}")

        return baseDirectory
    }

    private fun getOsmDroidCacheDirectory(): File {
        val cacheDirectory = if (osmDroidCachePath != null) {
            File(osmDroidCachePath)
        } else {
            val basePath = getOsmDroidBaseDirectory().path
            val file = getOrCreateDirectory(
                parent = basePath,
                child = DIRECTORY_NAME_CACHE
            ) ?: error("OSM directory creation error: $basePath/$DIRECTORY_NAME_CACHE")

            file
        }

        Timber.i("Using OSM Cache DIR: ${cacheDirectory.path}")

        return cacheDirectory
    }

}
