package hu.mostoha.mobile.android.huki.osmdroid.tilesource

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource

private const val TILE_SOURCE_NAME = "TuHu"
private const val MIN_ZOOM_LEVEL = 5
private const val MAX_ZOOM_LEVEL = 19
private const val TILE_SIZE_PX = 256
private const val FILE_NAME_ENDING = ".png"
private const val MAX_CONCURRENT_DOWNLOAD = 2
private const val POLICY_FLAGS = TileSourcePolicy.FLAG_NO_BULK or
    TileSourcePolicy.FLAG_NO_PREVENTIVE or
    TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
    TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED

object TuHuTileSource : XYTileSource(
    TILE_SOURCE_NAME,
    MIN_ZOOM_LEVEL,
    MAX_ZOOM_LEVEL,
    TILE_SIZE_PX,
    FILE_NAME_ENDING,
    arrayOf(
        "https://a.map.turistautak.hu/tiles/turistautak/",
        "https://b.map.turistautak.hu/tiles/turistautak/",
        "https://c.map.turistautak.hu/tiles/turistautak/",
    ),
    null,
    TileSourcePolicy(MAX_CONCURRENT_DOWNLOAD, POLICY_FLAGS)
)