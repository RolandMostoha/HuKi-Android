package hu.mostoha.mobile.android.huki.osmdroid.tilecache

import org.osmdroid.tileprovider.modules.DatabaseFileArchive
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.ITileSource

class AwsSqlTileWriter : SqlTileWriter() {

    companion object {
        /**
         * The indicator [ByteArray] for a not found tile.
         */
        val NOT_FOUND_BYTE_ARRAY = ByteArray(1).apply { set(0, Byte.MIN_VALUE) }
    }

    private val columns = arrayOf(DatabaseFileArchive.COLUMN_TILE, COLUMN_EXPIRES)

    /**
     * Returns true if the tile is not available.
     * Not available means that a tile request got a HTTP 404 Not Found response.
     */
    fun isTileNotAvailable(tileSource: ITileSource, mapTileIndex: Long): Boolean {
        if (!exists(tileSource, mapTileIndex)) {
            return false
        }

        val index = getIndex(mapTileIndex)
        val tileCursor = getTileCursor(getPrimaryKeyParameters(index, tileSource), columns)

        tileCursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val bits = cursor.getBlob(0)

                if (bits.contentEquals(NOT_FOUND_BYTE_ARRAY)) {
                    return true
                }
            }
        }

        return false
    }

}
