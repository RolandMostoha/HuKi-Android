package hu.mostoha.mobile.android.huki.extensions

import androidx.annotation.DimenRes
import androidx.annotation.Px
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toOsm
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.OffsetType
import hu.mostoha.mobile.android.huki.util.HUNGARY_BOUNDING_BOX
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox as DomainBoundingBox

fun BoundingBox.withMapViewOffsetPx(
    mapView: MapView,
    @Px top: Int = 0,
    @Px bottom: Int = 0,
    @Px left: Int = 0,
    @Px right: Int = 0,
    inverse: Boolean = false,
): BoundingBox {
    val scale = if (inverse) -1 else 1

    val topPx = scale * top
    val bottomPx = scale * bottom
    val leftPx = scale * left
    val rightPx = scale * right

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

fun BoundingBox.withMapViewOffset(
    mapView: MapView,
    @DimenRes top: Int,
    @DimenRes bottom: Int,
    @DimenRes left: Int,
    @DimenRes right: Int,
    inverse: Boolean = false,
): BoundingBox {
    val topPx = mapView.resources.getDimensionPixelSize(top)
    val bottomPx = mapView.resources.getDimensionPixelSize(bottom)
    val leftPx = mapView.resources.getDimensionPixelSize(left)
    val rightPx = mapView.resources.getDimensionPixelSize(right)

    return withMapViewOffsetPx(
        mapView = mapView,
        top = topPx,
        bottom = bottomPx,
        left = leftPx,
        right = rightPx,
        inverse = inverse
    )
}

@Suppress("ComplexMethod")
fun BoundingBox.withOffset(mapView: MapView, offsetType: OffsetType): BoundingBox {
    return withMapViewOffset(
        mapView = mapView,
        top = offsetType.top,
        bottom = offsetType.bottom,
        left = offsetType.left,
        right = offsetType.right,
    )
}

fun DomainBoundingBox.withOffset(mapView: MapView, offsetType: OffsetType): BoundingBox {
    return this.toOsm().withOffset(mapView, offsetType)
}

fun MapView.landscapeBoundingBox(landscape: LandscapeDetailsUiModel): BoundingBox {
    val baseBoundingBox = BoundingBox.fromGeoPoints(
        listOf<GeoPoint>()
            .plus(landscape.geometryUiModel.ways.flatMap { it.geoPoints })
            .plus(landscape.landscapeUiModel.destinations.map { it.location.toGeoPoint() })
    )
    val boundingBox = if (extraOffsetLandscapes.contains(landscape.landscapeUiModel.name.res)) {
        baseBoundingBox.withOffset(this, OffsetType.LANDSCAPE_EXTRA_OFFSET)
    } else {
        baseBoundingBox.withOffset(this, OffsetType.LANDSCAPE)
    }
    return boundingBox
}

fun MapView.allLandscapesBoundingBox(): BoundingBox {
    return HUNGARY_BOUNDING_BOX.toOsm().withMapViewOffsetPx(
        mapView = this,
        left = R.dimen.space_small.pixelSize(context),
        right = R.dimen.space_small.pixelSize(context),
        bottom = R.dimen.map_view_landscape_all_bottom_offset.pixelSize(context)
    )
}

private val extraOffsetLandscapes = listOf(
    R.string.landscape_budai_hegyseg,
    R.string.landscape_borzsony,
    R.string.landscape_cserhat,
    R.string.landscape_hortobagy,
    R.string.landscape_keszthelyi_hegyseg,
    R.string.landscape_vertes,
    R.string.landscape_zalai_dombsag,
    R.string.landscape_zempleni_hegyseg,
)
