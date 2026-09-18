package hu.mostoha.mobile.android.huki.osmdroid.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.TilesOverlay
import kotlin.math.ceil
import kotlin.math.pow

/**
 * Above the tile source's max zoom, draws the max zoom tiles scaled up on the canvas,
 * instead of letting osmdroid approximate a new bitmap for every overzoomed tile.
 */
class HikingTilesOverlay(
    tileProvider: MapTileProviderBase,
    context: Context,
) : TilesOverlay(tileProvider, context) {

    private val maxTileZoomLevel = tileProvider.tileSource.maximumZoomLevel.toDouble()

    override fun draw(canvas: Canvas, projection: Projection) {
        if (projection.zoomLevel <= maxTileZoomLevel) {
            super.draw(canvas, projection)
            return
        }

        val scale = 2.0.pow(projection.zoomLevel - maxTileZoomLevel)
        val maxZoomProjection = projection.toMaxZoomProjection(scale)

        canvas.save()
        canvas.translate(
            (projection.offsetX - scale * maxZoomProjection.offsetX).toFloat(),
            (projection.offsetY - scale * maxZoomProjection.offsetY).toFloat(),
        )
        canvas.scale(scale.toFloat(), scale.toFloat())
        super.draw(canvas, maxZoomProjection)
        canvas.restore()
    }

    override fun setViewPort(canvas: Canvas, projection: Projection): Boolean {
        val zoomLevel = projection.zoomLevel
        val viewPortProjection = if (zoomLevel > maxTileZoomLevel) {
            projection.toMaxZoomProjection(2.0.pow(zoomLevel - maxTileZoomLevel))
        } else {
            projection
        }

        return super.setViewPort(canvas, viewPortProjection)
    }

    private fun Projection.toMaxZoomProjection(scale: Double): Projection {
        if (zoomLevel <= maxTileZoomLevel) {
            return this
        }
        val screenRect = intrinsicScreenRect
        val width = ceil(screenRect.width() / scale).toInt() + 2
        val height = ceil(screenRect.height() / scale).toInt() + 2

        return getOffspring(maxTileZoomLevel, Rect(0, 0, width, height))
    }

}
