package hu.mostoha.mobile.android.huki.extensions

import androidx.annotation.DimenRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.toOsm
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox as DomainBoundingBox

private fun BoundingBox.withMapViewOffset(
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

@Suppress("ComplexMethod")
fun BoundingBox.withOffset(mapView: MapView, offsetType: OffsetType): BoundingBox {
    return withMapViewOffset(
        mapView = mapView,
        top = when (offsetType) {
            OffsetType.TOP_SHEET -> R.dimen.map_view_top_sheet_top_offset
            OffsetType.OKT_ROUTES -> R.dimen.map_view_okt_routes_top_offset
            else -> R.dimen.map_view_default_top_offset
        },
        bottom = when (offsetType) {
            OffsetType.BOTTOM_SHEET -> R.dimen.map_view_bottom_sheet_bottom_offset
            OffsetType.TOP_SHEET -> R.dimen.map_view_top_sheet_bottom_offset
            OffsetType.LANDSCAPE -> R.dimen.map_view_landscape_bottom_sheet_bottom_offset
            OffsetType.OKT_ROUTES -> R.dimen.map_view_okt_routes_bottom_offset
            else -> R.dimen.map_view_default_bottom_offset
        },
        left = when (offsetType) {
            OffsetType.TOP_SHEET -> R.dimen.map_view_top_sheet_start_offset
            OffsetType.LANDSCAPE -> R.dimen.map_view_landscape_start_offset
            else -> R.dimen.map_view_default_start_offset
        },
        right = when (offsetType) {
            OffsetType.TOP_SHEET -> R.dimen.map_view_top_sheet_end_offset
            OffsetType.LANDSCAPE -> R.dimen.map_view_landscape_end_offset
            else -> R.dimen.map_view_default_end_offset
        }
    )
}

fun DomainBoundingBox.withOffset(mapView: MapView, offsetType: OffsetType): BoundingBox {
    return this.toOsm().withOffset(mapView, offsetType)
}

enum class OffsetType {
    DEFAULT,
    BOTTOM_SHEET,
    TOP_SHEET,
    LANDSCAPE,
    OKT_ROUTES,
}
