package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.formatHoursAndMinutes
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGpxWaypoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.AltitudeUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.WaypointUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileUrlProvider
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_OPEN
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersAdapterItem
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.WaypointType
import hu.mostoha.mobile.android.huki.util.calculateDirectionArrows
import io.mockk.mockk
import org.junit.Test
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class LayersUiModelMapperTest {

    private val mapper = LayersUiModelMapper()

    private val urlProvider = mockk<AwsHikingTileUrlProvider>()

    @Test
    fun `Given Mapnik base layer and null hiking layer, when mapLayerAdapterItems, then adapter items with selected Mapnik returns`() {
        val baseLayer = BaseLayer.MAPNIK
        val hikingLayer = null
        val gpxDetails = null

        val adapterItems = mapper.mapLayerAdapterItems(baseLayer, hikingLayer, gpxDetails)

        assertThat(adapterItems).isEqualTo(
            listOf(
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_base_layers_header,
                    isCloseVisible = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MAPNIK,
                    titleRes = R.string.layers_mapnik_title,
                    drawableRes = R.drawable.ic_layers_mapnik,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.OPEN_TOPO,
                    titleRes = R.string.layers_open_topo_title,
                    drawableRes = R.drawable.ic_layers_open_topo,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.TUHU,
                    titleRes = R.string.layers_tuhu_title,
                    drawableRes = R.drawable.ic_layers_tuhu,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GOOGLE_SATELLITE,
                    titleRes = R.string.layers_google_satellite_title,
                    drawableRes = R.drawable.ic_layers_google_satellite,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MERRETEKERJEK,
                    titleRes = R.string.layers_merretekerjek_title,
                    drawableRes = R.drawable.ic_layers_merretekerjek,
                    isSelected = false
                ),
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_hiking_layers_header,
                    isCloseVisible = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                    titleRes = R.string.layers_hiking_hungarian_title,
                    drawableRes = R.drawable.ic_layers_hiking,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GPX,
                    titleRes = R.string.layers_gpx_title,
                    drawableRes = R.drawable.ic_layers_gpx,
                    isSelected = false
                )
            )
        )
    }

    @Test
    fun `Given OpenTopo base layer and null hiking layer, when mapLayerAdapterItems, then adapter items with selected OpenTopo returns`() {
        val baseLayer = BaseLayer.OPEN_TOPO
        val hikingLayer = null
        val gpxDetails = null

        val adapterItems = mapper.mapLayerAdapterItems(baseLayer, hikingLayer, gpxDetails)

        assertThat(adapterItems).isEqualTo(
            listOf(
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_base_layers_header,
                    isCloseVisible = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MAPNIK,
                    titleRes = R.string.layers_mapnik_title,
                    drawableRes = R.drawable.ic_layers_mapnik,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.OPEN_TOPO,
                    titleRes = R.string.layers_open_topo_title,
                    drawableRes = R.drawable.ic_layers_open_topo,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.TUHU,
                    titleRes = R.string.layers_tuhu_title,
                    drawableRes = R.drawable.ic_layers_tuhu,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GOOGLE_SATELLITE,
                    titleRes = R.string.layers_google_satellite_title,
                    drawableRes = R.drawable.ic_layers_google_satellite,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MERRETEKERJEK,
                    titleRes = R.string.layers_merretekerjek_title,
                    drawableRes = R.drawable.ic_layers_merretekerjek,
                    isSelected = false
                ),
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_hiking_layers_header,
                    isCloseVisible = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                    titleRes = R.string.layers_hiking_hungarian_title,
                    drawableRes = R.drawable.ic_layers_hiking,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GPX,
                    titleRes = R.string.layers_gpx_title,
                    drawableRes = R.drawable.ic_layers_gpx,
                    isSelected = false
                )
            )
        )
    }

    @Test
    fun `Given Mapnik base layer and hiking layer, when mapLayerAdapterItems, then adapter items with selected OpenTopo and Hiking Layer returns`() {
        val baseLayer = BaseLayer.MAPNIK
        val hikingLayer = HikingLayer(LayerType.HUNGARIAN_HIKING_LAYER, AwsHikingTileSource(urlProvider, emptyList()))
        val gpxDetails = null

        val adapterItems = mapper.mapLayerAdapterItems(baseLayer, hikingLayer, gpxDetails)

        assertThat(adapterItems).isEqualTo(
            listOf(
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_base_layers_header,
                    isCloseVisible = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MAPNIK,
                    titleRes = R.string.layers_mapnik_title,
                    drawableRes = R.drawable.ic_layers_mapnik,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.OPEN_TOPO,
                    titleRes = R.string.layers_open_topo_title,
                    drawableRes = R.drawable.ic_layers_open_topo,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.TUHU,
                    titleRes = R.string.layers_tuhu_title,
                    drawableRes = R.drawable.ic_layers_tuhu,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GOOGLE_SATELLITE,
                    titleRes = R.string.layers_google_satellite_title,
                    drawableRes = R.drawable.ic_layers_google_satellite,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MERRETEKERJEK,
                    titleRes = R.string.layers_merretekerjek_title,
                    drawableRes = R.drawable.ic_layers_merretekerjek,
                    isSelected = false
                ),
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_hiking_layers_header,
                    isCloseVisible = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                    titleRes = R.string.layers_hiking_hungarian_title,
                    drawableRes = R.drawable.ic_layers_hiking,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GPX,
                    titleRes = R.string.layers_gpx_title,
                    drawableRes = R.drawable.ic_layers_gpx,
                    isSelected = false
                )
            )
        )
    }

    @Test
    fun `Given layers with GPX details, when mapLayerAdapterItems, then adapter items with selected GPX layer returns`() {
        val baseLayer = BaseLayer.MAPNIK
        val hikingLayer = null
        val gpxDetails = DEFAULT_GPX_DETAILS_UI_MODEL

        val adapterItems = mapper.mapLayerAdapterItems(baseLayer, hikingLayer, gpxDetails)

        assertThat(adapterItems).isEqualTo(
            listOf(
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_base_layers_header,
                    isCloseVisible = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MAPNIK,
                    titleRes = R.string.layers_mapnik_title,
                    drawableRes = R.drawable.ic_layers_mapnik,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.OPEN_TOPO,
                    titleRes = R.string.layers_open_topo_title,
                    drawableRes = R.drawable.ic_layers_open_topo,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.TUHU,
                    titleRes = R.string.layers_tuhu_title,
                    drawableRes = R.drawable.ic_layers_tuhu,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GOOGLE_SATELLITE,
                    titleRes = R.string.layers_google_satellite_title,
                    drawableRes = R.drawable.ic_layers_google_satellite,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MERRETEKERJEK,
                    titleRes = R.string.layers_merretekerjek_title,
                    drawableRes = R.drawable.ic_layers_merretekerjek,
                    isSelected = false
                ),
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_hiking_layers_header,
                    isCloseVisible = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                    titleRes = R.string.layers_hiking_hungarian_title,
                    drawableRes = R.drawable.ic_layers_hiking,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GPX,
                    titleRes = R.string.layers_gpx_title,
                    drawableRes = R.drawable.ic_layers_gpx,
                    isSelected = true
                )
            )
        )
    }

    @Test
    fun `Given layers with invisible GPX details, when mapLayerAdapterItems, then adapter items with unselected GPX layer returns`() {
        val baseLayer = BaseLayer.MAPNIK
        val hikingLayer = null
        val gpxDetails = DEFAULT_GPX_DETAILS_UI_MODEL.copy(isVisible = false)

        val adapterItems = mapper.mapLayerAdapterItems(baseLayer, hikingLayer, gpxDetails)

        assertThat(adapterItems).isEqualTo(
            listOf(
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_base_layers_header,
                    isCloseVisible = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MAPNIK,
                    titleRes = R.string.layers_mapnik_title,
                    drawableRes = R.drawable.ic_layers_mapnik,
                    isSelected = true
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.OPEN_TOPO,
                    titleRes = R.string.layers_open_topo_title,
                    drawableRes = R.drawable.ic_layers_open_topo,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.TUHU,
                    titleRes = R.string.layers_tuhu_title,
                    drawableRes = R.drawable.ic_layers_tuhu,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GOOGLE_SATELLITE,
                    titleRes = R.string.layers_google_satellite_title,
                    drawableRes = R.drawable.ic_layers_google_satellite,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.MERRETEKERJEK,
                    titleRes = R.string.layers_merretekerjek_title,
                    drawableRes = R.drawable.ic_layers_merretekerjek,
                    isSelected = false
                ),
                LayersAdapterItem.Header(
                    titleRes = R.string.layers_hiking_layers_header,
                    isCloseVisible = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                    titleRes = R.string.layers_hiking_hungarian_title,
                    drawableRes = R.drawable.ic_layers_hiking,
                    isSelected = false
                ),
                LayersAdapterItem.Layer(
                    layerType = LayerType.GPX,
                    titleRes = R.string.layers_gpx_title,
                    drawableRes = R.drawable.ic_layers_gpx,
                    isSelected = false
                )
            )
        )
    }

    @Test
    fun `Given GPX details, when mapGpxDetails, then GPX Details UI model returns`() {
        val gpxDetails = DEFAULT_GPX_DETAILS
        val useSlopeColors = true

        val gpxDetailsUiModel = mapper.mapGpxDetails(gpxDetails, useSlopeColors)

        assertThat(gpxDetailsUiModel).isEqualTo(
            GpxDetailsUiModel(
                id = gpxDetails.id,
                fileUri = gpxDetails.fileUri,
                name = gpxDetails.fileName,
                geoPoints = gpxDetails.locations.map { it.toGeoPoint() },
                waypoints = listOf(
                    WaypointUiModel(
                        geoPoint = gpxDetails.gpxWaypoints.first().location.toGeoPoint(),
                        waypointType = WaypointType.INTERMEDIATE,
                    ),
                    WaypointUiModel(
                        geoPoint = gpxDetails.locations.first().toGeoPoint(),
                        waypointType = WaypointType.ROUND_TRIP,
                    )
                ),
                arrowGeoPoints = calculateDirectionArrows(gpxDetailsUiModel.geoPoints),
                boundingBox = BoundingBox
                    .fromGeoPoints(gpxDetails.locations.map { it.toGeoPoint() })
                    .toDomain(),
                travelTimeText = gpxDetails.travelTime.formatHoursAndMinutes().toMessage(),
                distanceText = DistanceFormatter.format(gpxDetails.distance),
                altitudeUiModel = AltitudeUiModel(
                    minAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.first),
                    maxAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.second),
                    uphillText = DistanceFormatter.format(gpxDetails.incline),
                    downhillText = DistanceFormatter.format(gpxDetails.decline),
                ),
                isVisible = true,
                useSlopeColors = useSlopeColors,
            )
        )
    }

    @Test
    fun `Given GPX details with open route, when mapGpxDetails, then GPX Details UI model returns`() {
        val gpxDetails = DEFAULT_GPX_DETAILS_OPEN
        val useSlopeColors = false

        val gpxDetailsUiModel = mapper.mapGpxDetails(gpxDetails, useSlopeColors)

        assertThat(gpxDetailsUiModel).isEqualTo(
            GpxDetailsUiModel(
                id = gpxDetails.id,
                fileUri = gpxDetails.fileUri,
                name = gpxDetails.fileName,
                geoPoints = gpxDetails.locations.map { it.toGeoPoint() },
                waypoints = listOf(
                    WaypointUiModel(
                        geoPoint = gpxDetails.gpxWaypoints.first().location.toGeoPoint(),
                        waypointType = WaypointType.INTERMEDIATE,
                    ),
                    WaypointUiModel(
                        geoPoint = gpxDetails.locations.first().toGeoPoint(),
                        waypointType = WaypointType.START,
                    ),
                    WaypointUiModel(
                        geoPoint = gpxDetails.locations.last().toGeoPoint(),
                        waypointType = WaypointType.END,
                    )
                ),
                arrowGeoPoints = calculateDirectionArrows(gpxDetailsUiModel.geoPoints),
                boundingBox = BoundingBox
                    .fromGeoPoints(gpxDetails.locations.map { it.toGeoPoint() })
                    .toDomain(),
                travelTimeText = gpxDetails.travelTime.formatHoursAndMinutes().toMessage(),
                distanceText = DistanceFormatter.format(gpxDetails.distance),
                altitudeUiModel = AltitudeUiModel(
                    minAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.first),
                    maxAltitudeText = DistanceFormatter.format(gpxDetails.altitudeRange.second),
                    uphillText = DistanceFormatter.format(gpxDetails.incline),
                    downhillText = DistanceFormatter.format(gpxDetails.decline),
                ),
                isVisible = true,
                useSlopeColors = useSlopeColors
            )
        )
    }

    @Test
    fun `Given GPX details without altitude range, when mapGpxDetails, then GPX Details UI model with null altitude UI model returns`() {
        val gpxDetails = DEFAULT_GPX_DETAILS.copy(altitudeRange = 0 to 0)
        val useSlopeColors = true

        val gpxDetailsUiModel = mapper.mapGpxDetails(gpxDetails, useSlopeColors)

        assertThat(gpxDetailsUiModel).isEqualTo(
            GpxDetailsUiModel(
                id = gpxDetails.id,
                fileUri = gpxDetails.fileUri,
                name = gpxDetails.fileName,
                geoPoints = gpxDetails.locations.map { it.toGeoPoint() },
                waypoints = listOf(
                    WaypointUiModel(
                        geoPoint = gpxDetails.gpxWaypoints.first().location.toGeoPoint(),
                        waypointType = WaypointType.INTERMEDIATE,
                    ),
                    WaypointUiModel(
                        geoPoint = gpxDetails.locations.first().toGeoPoint(),
                        waypointType = WaypointType.ROUND_TRIP,
                    )
                ),
                arrowGeoPoints = calculateDirectionArrows(gpxDetailsUiModel.geoPoints),
                boundingBox = BoundingBox
                    .fromGeoPoints(gpxDetails.locations.map { it.toGeoPoint() })
                    .toDomain(),
                travelTimeText = gpxDetails.travelTime.formatHoursAndMinutes().toMessage(),
                distanceText = DistanceFormatter.format(gpxDetails.distance),
                altitudeUiModel = null,
                isVisible = true,
                useSlopeColors = useSlopeColors
            )
        )
    }

    companion object {
        private val DEFAULT_GPX_DETAILS = GpxDetails(
            id = UUID.randomUUID().toString(),
            fileUri = "file://dera_szurdok.gpx",
            fileName = "dera_szurdok.gpx",
            gpxWaypoints = listOf(DEFAULT_GPX_WAY_CLOSED.first().toLocation().toGpxWaypoint()),
            locations = DEFAULT_GPX_WAY_CLOSED.map { Location(it.first, it.second) },
            travelTime = 2L.hours,
            distance = 15000,
            altitudeRange = 300 to 800,
            incline = 500,
            decline = 300,
        )
        private val DEFAULT_GPX_DETAILS_OPEN = GpxDetails(
            id = UUID.randomUUID().toString(),
            fileUri = "file://dera_szurdok.gpx",
            fileName = "dera_szurdok.gpx",
            gpxWaypoints = listOf(DEFAULT_GPX_WAY_OPEN.first().toLocation().toGpxWaypoint()),
            locations = DEFAULT_GPX_WAY_OPEN.map { Location(it.first, it.second) },
            travelTime = 2L.hours,
            distance = 15000,
            altitudeRange = 300 to 800,
            incline = 500,
            decline = 300,
        )
        private val DEFAULT_GPX_DETAILS_UI_MODEL = GpxDetailsUiModel(
            id = DEFAULT_GPX_DETAILS.id,
            fileUri = "file://dera_szurdok.gpx",
            name = "dera_szurdok",
            waypoints = listOf(
                WaypointUiModel(
                    geoPoint = DEFAULT_GPX_WAY_CLOSED.first().toLocation().toGeoPoint(),
                    waypointType = WaypointType.INTERMEDIATE
                )
            ),
            arrowGeoPoints = emptyList(),
            geoPoints = DEFAULT_GPX_WAY_CLOSED.map { GeoPoint(it.first, it.second) },
            boundingBox = BoundingBox
                .fromGeoPoints(DEFAULT_GPX_WAY_CLOSED.map { GeoPoint(it.first, it.second) })
                .toDomain(),
            travelTimeText = Message.Text("02:00"),
            distanceText = Message.Res(R.string.default_distance_template_m, listOf(5)),
            altitudeUiModel = null,
            isVisible = true,
            useSlopeColors = true,
        )
    }

}
