package hu.mostoha.mobile.android.huki.model.domain

/**
 * Model that represents an x,y limit for a zoom level to avoid the unnecessary requests of tiles.
 */
data class TileZoomRange(
    val zoom: Int,
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int
)
