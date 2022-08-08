package hu.mostoha.mobile.android.huki.osmdroid.tileprovider

import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.tilesource.ITileSource

class AwsMapTileDownloader(
    tileSource: ITileSource,
    tileWriter: IFilesystemCache,
    networkAvailablityCheck: INetworkAvailablityCheck
) : MapTileDownloader(tileSource, tileWriter, networkAvailablityCheck) {

    init {
        setTileDownloader(AwsTileDownloader())
    }

}
