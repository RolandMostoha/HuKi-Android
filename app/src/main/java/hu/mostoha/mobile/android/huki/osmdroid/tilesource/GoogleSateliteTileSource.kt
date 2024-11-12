package hu.mostoha.mobile.android.huki.osmdroid.tilesource

import androidx.core.net.toUri
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

private const val TILE_SOURCE_NAME = "Google Satellite"
private const val MIN_ZOOM_LEVEL = 0
private const val MAX_ZOOM_LEVEL = 19
private const val TILE_SIZE_PX = 256
private const val FILE_NAME_ENDING = ".png"
private const val MAX_CONCURRENT_DOWNLOAD = 2
private const val POLICY_FLAGS = TileSourcePolicy.FLAG_NO_BULK or
    TileSourcePolicy.FLAG_NO_PREVENTIVE or
    TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
    TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED

object GoogleSateliteTileSource : XYTileSource(
    TILE_SOURCE_NAME,
    MIN_ZOOM_LEVEL,
    MAX_ZOOM_LEVEL,
    TILE_SIZE_PX,
    FILE_NAME_ENDING,
    arrayOf(
        "https://mt0.google.com/vt/lyrs=s",
        "https://mt1.google.com/vt/lyrs=s",
        "https://mt2.google.com/vt/lyrs=s",
        "https://mt3.google.com/vt/lyrs=s",
    ),
    null,
    TileSourcePolicy(MAX_CONCURRENT_DOWNLOAD, POLICY_FLAGS)
) {

    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)

        return baseUrl.toUri()
            .buildUpon()
            .appendQueryParameter("x", x.toString())
            .appendQueryParameter("y", y.toString())
            .appendQueryParameter("z", zoom.toString())
            .build()
            .toString()
    }

}
