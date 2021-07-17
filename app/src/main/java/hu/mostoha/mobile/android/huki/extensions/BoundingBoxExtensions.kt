package hu.mostoha.mobile.android.huki.extensions

import androidx.annotation.DimenRes
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection


fun BoundingBox.withOffset(
    mapView: MapView,
    @DimenRes top: Int,
    @DimenRes bottom: Int,
    @DimenRes left: Int,
    @DimenRes right: Int
): BoundingBox {
    val topPx = mapView.context.resources.getDimensionPixelSize(top)
    val bottomPx = mapView.context.resources.getDimensionPixelSize(bottom)
    val leftPx = mapView.context.resources.getDimensionPixelSize(left)
    val rightPx = mapView.context.resources.getDimensionPixelSize(right)

    val width = mapView.width
    val height = mapView.height

    val nextZoom = MapView.getTileSystem().getBoundingBoxZoom(
        this,
        width - (leftPx + rightPx),
        height - (topPx + bottomPx)
    )
    val projection = Projection(
        nextZoom, width, height,
        centerWithDateLine,
        mapView.mapOrientation,
        mapView.isHorizontalMapRepetitionEnabled, mapView.isVerticalMapRepetitionEnabled,
        mapView.mapCenterOffsetX, mapView.mapCenterOffsetY
    )

    val northWest = projection.fromPixels(0, 0)
    val southEast = projection.fromPixels(width, height)

    val lonPerPx = (southEast.longitude - northWest.longitude) / width
    val latPerPx = (southEast.latitude - northWest.latitude) / height

    return BoundingBox(
        latNorth - topPx * latPerPx,
        lonEast + rightPx * lonPerPx,
        latSouth + bottomPx * latPerPx,
        lonWest - leftPx * lonPerPx
    )
}
