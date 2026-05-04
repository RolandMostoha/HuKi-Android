package hu.mostoha.mobile.android.huki.osmdroid.tilesource

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex

private const val TILE_SOURCE_NAME = "Hiking-OSM-HU"
private const val MIN_ZOOM_LEVEL = 0
private const val MAX_ZOOM_LEVEL = 17
private const val TILE_SIZE_PX = 256
private const val FILE_NAME_ENDING = ".png"
private val TILE_SOURCE_URLS = listOf(
    "https://a.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
    "https://b.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
    "https://c.tile.openstreetmap.hu/tt/{z}/{x}/{y}.png",
)
private const val MAX_CONCURRENT_DOWNLOAD = 3
private const val POLICY_FLAGS = TileSourcePolicy.FLAG_NO_BULK or
    TileSourcePolicy.FLAG_NO_PREVENTIVE or
    TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
    TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED

object HikingTileSource : XYTileSource(
    TILE_SOURCE_NAME,
    MIN_ZOOM_LEVEL,
    MAX_ZOOM_LEVEL,
    TILE_SIZE_PX,
    FILE_NAME_ENDING,
    TILE_SOURCE_URLS.toTypedArray(),
    null,
    TileSourcePolicy(MAX_CONCURRENT_DOWNLOAD, POLICY_FLAGS)
) {

    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)

        return HikingTileSource.baseUrl
            .replace("{z}", zoom.toString())
            .replace("{x}", x.toString())
            .replace("{y}", y.toString())
    }

}
