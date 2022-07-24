package hu.mostoha.mobile.android.huki.osmdroid.tiles

import android.database.sqlite.SQLiteDatabase
import org.osmdroid.tileprovider.modules.IArchiveFile
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.util.MapTileIndex
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

/**
 * Supports OsmAnd specific SQLite tile format.
 *
 * https://docs.osmand.net/ru/main@latest/development/osmand-file-formats/osmand-sqlite
 */
class OsmAndSqliteFileArchive : IArchiveFile {

    companion object {
        const val TABLE_TILES = "tiles"
        const val COLUMN_X = "x"
        const val COLUMN_Y = "y"
        const val COLUMN_ZOOM = "z"
        const val COLUMN_TILE_IMAGE = "image"
        const val MAXIMUM_ZOOM_LEVEL = 17
    }

    private lateinit var database: SQLiteDatabase

    override fun init(pFile: File) {
        database = SQLiteDatabase.openDatabase(
            pFile.absolutePath,
            null,
            SQLiteDatabase.NO_LOCALIZED_COLLATORS or SQLiteDatabase.OPEN_READONLY
        )
    }

    @Suppress("TooGenericExceptionCaught")
    override fun getInputStream(pTileSource: ITileSource, pMapTileIndex: Long): InputStream? {
        try {
            var inputStream: InputStream? = null

            val cursor = database.query(
                TABLE_TILES,
                arrayOf(COLUMN_TILE_IMAGE),
                "$COLUMN_X = ? and $COLUMN_Y = ? and $COLUMN_ZOOM = ?",
                arrayOf(
                    MapTileIndex.getX(pMapTileIndex).toString(),
                    MapTileIndex.getY(pMapTileIndex).toString(),
                    invertZoom(MapTileIndex.getZoom(pMapTileIndex)).toString()
                ),
                null,
                null,
                null
            )
            if (cursor.count != 0) {
                cursor.moveToFirst()
                inputStream = ByteArrayInputStream(cursor.getBlob(0))
            }
            cursor.close()

            return inputStream
        } catch (throwable: Throwable) {
            Timber.w(throwable, "Error getting db stream: ${MapTileIndex.toString(pMapTileIndex)}")
        }
        return null
    }

    private fun invertZoom(zoom: Int): Int = MAXIMUM_ZOOM_LEVEL - zoom

    override fun getTileSources(): Set<String> = emptySet()

    override fun setIgnoreTileSource(pIgnoreTileSource: Boolean) {
        // No-op
    }

    override fun close() {
        database.close()
    }

}
