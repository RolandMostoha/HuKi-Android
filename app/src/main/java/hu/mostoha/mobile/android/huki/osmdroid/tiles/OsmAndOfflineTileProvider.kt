package hu.mostoha.mobile.android.huki.osmdroid.tiles

import org.osmdroid.tileprovider.IMapTileProviderCallback
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.modules.MapTileApproximater
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import java.io.File

/**
 * Offline tile provider that supports OsmAnd specific SQLite tile format.
 */
class OsmAndOfflineTileProvider(
    registerReceiver: SimpleRegisterReceiver,
    val file: File
) : MapTileProviderArray(FileBasedTileSource.getSource(file.name), registerReceiver), IMapTileProviderCallback {

    private var fileArchive: OsmAndSqliteFileArchive = OsmAndSqliteFileArchive()

    init {
        fileArchive.init(file)

        val mapTileFileArchiveProvider = MapTileFileArchiveProvider(
            registerReceiver,
            tileSource,
            arrayOf(fileArchive)
        )
        mTileProviderList.add(mapTileFileArchiveProvider)

        val approximationProvider = MapTileApproximater()
        mTileProviderList.add(approximationProvider)
        approximationProvider.addProvider(mapTileFileArchiveProvider)
    }

    override fun detach() {
        fileArchive.close()

        super.detach()
    }

    override fun isDowngradedMode(pMapTileIndex: Long): Boolean = true

}
