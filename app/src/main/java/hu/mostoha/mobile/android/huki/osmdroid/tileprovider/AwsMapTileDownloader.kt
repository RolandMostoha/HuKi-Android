package hu.mostoha.mobile.android.huki.osmdroid.tileprovider

import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.tilesource.ITileSource

class AwsMapTileDownloader(
    tileSource: ITileSource,
    tileWriter: IFilesystemCache,
    networkAvailablityCheck: INetworkAvailablityCheck
) : MapTileDownloader(tileSource, tileWriter, networkAvailablityCheck, MAX_THREAD_POOL_SIZE, MAX_PENDING_QUEUE_SIZE) {

    companion object {
        const val MAX_THREAD_POOL_SIZE = 3
        const val MAX_PENDING_QUEUE_SIZE = 3
    }

    init {
        setTileDownloader(AwsTileDownloader())
    }

}
