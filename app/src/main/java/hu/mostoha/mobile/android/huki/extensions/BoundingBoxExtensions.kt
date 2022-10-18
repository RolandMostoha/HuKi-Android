package hu.mostoha.mobile.android.huki.extensions

import androidx.annotation.DimenRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox as DomainBoundingBox

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

    if (width == 0 || height == 0) {
        return this
    }

    val nextZoom = MapView.getTileSystem().getBoundingBoxZoom(
        this,
        width - (leftPx + rightPx),
        height - (topPx + bottomPx)
    )
    val projection = Projection(
        nextZoom,
        width,
        height,
        centerWithDateLine,
        mapView.mapOrientation,
        mapView.isHorizontalMapRepetitionEnabled,
        mapView.isVerticalMapRepetitionEnabled,
        mapView.mapCenterOffsetX,
        mapView.mapCenterOffsetY
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

fun BoundingBox.withDefaultOffset(mapView: MapView): BoundingBox {
    return withOffset(
        mapView,
        R.dimen.home_map_view_top_offset,
        R.dimen.home_map_view_bottom_offset,
        R.dimen.home_map_view_start_offset,
        R.dimen.home_map_view_end_offset
    )
}

fun DomainBoundingBox.withDefaultOffset(mapView: MapView): BoundingBox {
    return this.toOsmBoundingBox().withOffset(
        mapView,
        R.dimen.home_map_view_top_offset,
        R.dimen.home_map_view_bottom_offset,
        R.dimen.home_map_view_start_offset,
        R.dimen.home_map_view_end_offset
    )
}
