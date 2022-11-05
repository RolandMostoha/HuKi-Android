package hu.mostoha.mobile.android.huki.osmdroid.tileprovider

import android.content.Context
import hu.mostoha.mobile.android.huki.osmdroid.tilecache.AwsMapTileSqlCacheProvider
import hu.mostoha.mobile.android.huki.osmdroid.tilecache.AwsSqlTileWriter
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MapTileApproximater
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.MapTileAreaBorderComputer
import org.osmdroid.util.MapTileAreaZoomComputer

/**
 * An offline-first tile provider which supports:
 *  - Increased thread pool and pending queue size
 *  - Not available (HTTP 404) tile caching
 */
class AwsMapTileProviderBasic(
    context: Context,
    tileSource: ITileSource,
    registerReceiver: SimpleRegisterReceiver = SimpleRegisterReceiver(context)
) : MapTileProviderArray(tileSource, registerReceiver) {

    companion object {
        private const val MAX_THREAD_POOL_SIZE = 3
        private const val MAX_PENDING_QUEUE_SIZE = 3
    }

    init {
        val tileWriter = AwsSqlTileWriter()

        val cacheProvider = AwsMapTileSqlCacheProvider(registerReceiver, tileSource, context, tileWriter)
        mTileProviderList.add(cacheProvider)

        val mapTileDownloader = MapTileDownloader(
            tileSource,
            tileWriter,
            NetworkAvailabliltyCheck(context),
            MAX_THREAD_POOL_SIZE,
            MAX_PENDING_QUEUE_SIZE
        )
        mapTileDownloader.setTileDownloader(AwsTileDownloader())
        mTileProviderList.add(mapTileDownloader)

        val approximationProvider = MapTileApproximater()
        approximationProvider.addProvider(cacheProvider)
        mTileProviderList.add(approximationProvider)

        // Protected-cache-tile computers
        tileCache.protectedTileComputers.add(MapTileAreaZoomComputer(-1))
        tileCache.protectedTileComputers.add(MapTileAreaBorderComputer(1))
        tileCache.setAutoEnsureCapacity(false)
        tileCache.setStressedMemory(false)

        // Pre-cache providers
        tileCache.preCache.addProvider(cacheProvider)
        tileCache.preCache.addProvider(mapTileDownloader)

        // Tiles currently being processed
        tileCache.protectedTileContainers.add(this)
    }

    /**
     * Sets to downgraded mode to avoid infinite loop of loading scaled images.
     * https://github.com/osmdroid/osmdroid/issues/1519
     */
    override fun isDowngradedMode(pMapTileIndex: Long): Boolean {
        return true
    }

}
