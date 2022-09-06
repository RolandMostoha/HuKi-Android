package hu.mostoha.mobile.android.huki.osmdroid.tilecache

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import hu.mostoha.mobile.android.huki.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.CantContinueException
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.util.Counters
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.MapTileIndex
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * A cache provider that loads a 256x256 transparent image if the tile is not available (HTTP 404 Not Found).
 */
class AwsMapTileSqlCacheProvider(
    registerReceiver: SimpleRegisterReceiver,
    tileSource: ITileSource,
    private val context: Context,
    private val tileWriter: AwsSqlTileWriter
) : MapTileFileStorageProviderBase(
    registerReceiver,
    Configuration.getInstance().tileFileSystemThreads.toInt(),
    Configuration.getInstance().tileFileSystemMaxQueueSize.toInt(),
) {

    private val tileSourceReference = AtomicReference<ITileSource>().apply { set(tileSource) }

    override fun getUsesDataConnection(): Boolean {
        return false
    }

    override fun getName(): String {
        return "AWS SQL Cache Archive Provider"
    }

    override fun getThreadGroupName(): String {
        return "AwsSqlCache"
    }

    override fun getTileLoader(): TileLoader {
        return AwsTileLoader()
    }

    override fun getMinimumZoomLevel(): Int {
        return tileSourceReference.get().minimumZoomLevel
    }

    override fun getMaximumZoomLevel(): Int {
        return tileSourceReference.get().maximumZoomLevel
    }

    override fun onMediaUnmounted() {
        tileWriter.onDetach()
    }

    override fun setTileSource(pTileSource: ITileSource) {
        tileSourceReference.set(pTileSource)
    }

    override fun detach() {
        tileWriter.onDetach()

        super.detach()
    }

    inner class AwsTileLoader : TileLoader() {

        @Suppress("TooGenericExceptionCaught")
        @Throws(CantContinueException::class)
        override fun loadTile(pMapTileIndex: Long): Drawable? {
            return try {
                val tileSource = tileSourceReference.get()

                val result = if (tileWriter.isTileNotAvailable(tileSource, pMapTileIndex)) {
                    ContextCompat.getDrawable(context, R.drawable.tile_256x256_transparent)!!
                } else {
                    tileWriter.loadTile(tileSource, pMapTileIndex)
                }

                if (result == null) {
                    Counters.fileCacheMiss++
                } else {
                    Counters.fileCacheHit++
                }

                result
            } catch (lowMemoryException: BitmapTileSourceBase.LowMemoryException) {
                // Low memory so empty the queue
                Timber.w(lowMemoryException, "LowMemory downloading MapTile: ${MapTileIndex.toString(pMapTileIndex)}")
                Counters.fileCacheOOM++
                throw CantContinueException(lowMemoryException)
            } catch (throwable: Throwable) {
                Timber.e(throwable, "Error loading tile")
                null
            }
        }

    }

}
