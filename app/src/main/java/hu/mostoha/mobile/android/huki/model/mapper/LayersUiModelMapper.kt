package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.ui.GpxAltitudeUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapterItem
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
        val locations = gpxDetails.locations
        val geoPoints = locations.map { it.toGeoPoint() }
        val altitudeRange = gpxDetails.altitudeRange

        return GpxDetailsUiModel(
            id = gpxDetails.id,
            name = gpxDetails.fileName,
            start = locations.first().toGeoPoint(),
            end = locations.last().toGeoPoint(),
            geoPoints = geoPoints,
            boundingBox = BoundingBox.fromGeoPoints(geoPoints).toDomainBoundingBox(),
            distanceText = DistanceFormatter.format(gpxDetails.distance),
            gpxAltitudeUiModel = if (altitudeRange.first != 0 && altitudeRange.second != 0) {
                GpxAltitudeUiModel(
                    minAltitudeText = DistanceFormatter.format(altitudeRange.first),
                    maxAltitudeText = DistanceFormatter.format(altitudeRange.second),
                    uphillText = DistanceFormatter.format(gpxDetails.incline),
                    downhillText = DistanceFormatter.format(gpxDetails.decline),
                )
            } else {
                null
            },
            isClosed = gpxDetails.isClosed,
            isVisible = true
        )
    }

}
