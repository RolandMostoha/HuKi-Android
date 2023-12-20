package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.AltitudeUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.WaypointUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapterItem
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.isCloseWithThreshold
import org.osmdroid.util.BoundingBox
import javax.inject.Inject

class LayersUiModelMapper @Inject constructor() {

    fun mapLayerAdapterItems(
        baseLayer: BaseLayer,
        hikingLayer: HikingLayer?,
        gpxDetails: GpxDetailsUiModel?
    ): List<LayersAdapterItem> {
        return listOf(
            LayersAdapterItem.Header(R.string.layers_base_layers_header),
            LayersAdapterItem.Layer(
                layerType = LayerType.MAPNIK,
                titleRes = R.string.layers_mapnik_title,
                drawableRes = R.drawable.ic_layers_mapnik,
                isSelected = baseLayer.layerType == LayerType.MAPNIK
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.OPEN_TOPO,
                titleRes = R.string.layers_open_topo_title,
                drawableRes = R.drawable.ic_layers_open_topo,
                isSelected = baseLayer.layerType == LayerType.OPEN_TOPO
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.TUHU,
                titleRes = R.string.layers_tuhu_title,
                drawableRes = R.drawable.ic_layers_tuhu,
                isSelected = baseLayer.layerType == LayerType.TUHU
            ),
            LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
            LayersAdapterItem.Layer(
                layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                titleRes = R.string.layers_hiking_hungarian_title,
                drawableRes = R.drawable.ic_layers_hiking,
                isSelected = hikingLayer?.layerType == LayerType.HUNGARIAN_HIKING_LAYER
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.GPX,
                titleRes = R.string.layers_gpx_title,
                drawableRes = R.drawable.ic_layers_gpx,
                isSelected = gpxDetails != null && gpxDetails.isVisible
            )
        )
    }

    fun mapGpxDetails(gpxDetails: GpxDetails): GpxDetailsUiModel {
        val geoPoints = gpxDetails.locations.map { it.toGeoPoint() }

        val edgeWaypoints = if (geoPoints.size >= 2) {
            val startLocation = geoPoints.first().toLocation()
            val endLocation = geoPoints.last().toLocation()
            if (startLocation.isCloseWithThreshold(endLocation)) {
                listOf(
                    WaypointUiModel(
                        startLocation.toGeoPoint(),
                        waypointType = WaypointType.END,
                    )
                )
            } else {
                listOf(
                    WaypointUiModel(
                        startLocation.toGeoPoint(),
                        waypointType = WaypointType.START,
                    ),
                    WaypointUiModel(
                        endLocation.toGeoPoint(),
                        waypointType = WaypointType.END,
                    ),
                )
            }
        } else {
            emptyList()
        }

        val gpxWaypoints = gpxDetails.gpxWaypoints.map { waypoint ->
            WaypointUiModel(
                geoPoint = waypoint.location.toGeoPoint(),
                waypointType = WaypointType.INTERMEDIATE,
                name = waypoint.name?.toMessage(),
                description = waypoint.description?.toMessage(),
            )
        }

        return GpxDetailsUiModel(
            id = gpxDetails.id,
            fileUri = gpxDetails.fileUri,
            name = gpxDetails.fileName,
            geoPoints = geoPoints,
            waypoints = gpxWaypoints + edgeWaypoints,
            boundingBox = if (geoPoints.size >= 2) {
                BoundingBox.fromGeoPoints(geoPoints).toDomain()
            } else {
                BoundingBox.fromGeoPoints(gpxWaypoints.map { it.geoPoint }).toDomain()
            },
            travelTimeText = if (gpxDetails.travelTime.inWholeSeconds > 0) {
                gpxDetails.travelTime.formatHoursAndMinutes().toMessage()
            } else {
                null
            },
            distanceText = if (gpxDetails.distance > 0) {
                DistanceFormatter.format(gpxDetails.distance)
            } else {
                null
            },
            altitudeUiModel = if (gpxDetails.altitudeRange.first != 0 && gpxDetails.altitudeRange.second != 0) {
                AltitudeUiModel(
                    minAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.first),
                    maxAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.second),
                    uphillText = DistanceFormatter.format(gpxDetails.incline),
                    downhillText = DistanceFormatter.format(gpxDetails.decline),
                )
            } else {
                null
            },
            isVisible = true
        )
    }

}
