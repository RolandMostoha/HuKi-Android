package hu.mostoha.mobile.android.huki.osmdroid.tiles

import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.MapTileIndex
import javax.inject.Inject

data class AwsHikingTileSource @Inject constructor(
    private val tileUrlProvider: HikingTileUrlProvider,
    private val tileZoomRanges: List<TileZoomRange>
) : XYTileSource(
    TILE_SOURCE_NAME,
    MIN_ZOOM_LEVEL,
    MAX_ZOOM_LEVEL,
    TILE_SIZE_PX,
    FILE_NAME_ENDING,
    emptyArray()
) {

    companion object {
        private const val TILE_SOURCE_NAME = "HuKi-Online"
        private const val MIN_ZOOM_LEVEL = 6
        private const val MAX_ZOOM_LEVEL = 17
        private const val TILE_SIZE_PX = 256
        private const val FILE_NAME_ENDING = ".png"
    }

    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)

        val isZoomOutOfRange = tileZoomRanges.isNotEmpty() && zoom !in tileZoomRanges.map { it.zoom }
        val tileZoomRange = tileZoomRanges.firstOrNull { it.zoom == zoom }
        val isXOutOfRange = tileZoomRange != null && x !in tileZoomRange.minX..tileZoomRange.maxX
        val isYOutOfRange = tileZoomRange != null && y !in tileZoomRange.minY..tileZoomRange.maxY

        if (isZoomOutOfRange || isXOutOfRange || isYOutOfRange) {
            return ""
        }

        val storageKey = "$zoom/$x/$y$mImageFilenameEnding"

        return tileUrlProvider.getHikingTileUrl(storageKey)
    }

}
