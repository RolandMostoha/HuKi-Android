package hu.mostoha.mobile.android.huki.osmdroid.tileprovider

import android.content.Context
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.tilesource.ITileSource

class AwsMapTileProviderBasic(context: Context, tileSource: ITileSource) : MapTileProviderBasic(context, tileSource) {

    override fun createDownloaderProvider(
        aNetworkAvailablityCheck: INetworkAvailablityCheck,
        pTileSource: ITileSource
    ): MapTileDownloader {
        return AwsMapTileDownloader(pTileSource, tileWriter, aNetworkAvailablityCheck)
    }

}
