package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoints
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.util.calculateCenter
import org.junit.Assert.assertThrows
import org.junit.Test
import org.osmdroid.util.GeoPoint

class HomeUiModelMapperTest {

    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val placeMapper = PlaceDomainUiMapper(hikingRouteRelationMapper)
    private val mapper = HomeUiModelMapper(placeMapper)

    @Test
    fun `Given landscape domain model, when mapLandscapes, then correct LandscapeUiModel returns`() {
        val landscape = DEFAULT_LANDSCAPE

        val places = mapper.mapLandscapes(listOf(landscape))

        assertThat(places).isEqualTo(
            listOf(
                LandscapeUiModel(
                    osmId = landscape.osmId,
                    osmType = landscape.osmType,
                    name = landscape.nameRes.toMessage(),
                    geoPoint = landscape.center.toGeoPoint(),
                    iconRes = R.drawable.ic_landscapes_mountain_medium,
                    markerRes = R.drawable.ic_marker_landscapes_mountain_medium,
                )
            )
        )
    }

    @Test
    fun `Given landscape domain model with null kirandulastippek and termeszetjaro tag, when mapLandscapes, then correct LandscapeUiModel returns with default URL`() {
        val landscape = DEFAULT_LANDSCAPE.copy(
            kirandulastippekTag = null,
            termeszetjaroTag = null,
        )

        val places = mapper.mapLandscapes(listOf(landscape))

        assertThat(places).isEqualTo(
            listOf(
                LandscapeUiModel(
                    osmId = landscape.osmId,
                    osmType = landscape.osmType,
                    name = landscape.nameRes.toMessage(),
                    geoPoint = landscape.center.toGeoPoint(),
                    iconRes = R.drawable.ic_landscapes_mountain_medium,
                    markerRes = R.drawable.ic_marker_landscapes_mountain_medium,
                )
            )
        )
    }

    @Test
    fun `Given landscape ui model and geometry, when mapLandscapeDetails, then correct LandscapeDetailSUiModel returns`() {
        val landscape = DEFAULT_LANDSCAPE
        val landscapeUiModel = mapper.mapLandscapes(listOf(landscape)).first()

        val places = mapper.mapLandscapeDetails(landscapeUiModel, DEFAULT_CLOSED_WAY_GEOMETRY)

        assertThat(places).isEqualTo(
            LandscapeDetailsUiModel(
                landscapeUiModel = landscapeUiModel,
                geometryUiModel = GeometryUiModel.Relation(
                    ways = listOf(
                        GeometryUiModel.Way(
                            osmId = DEFAULT_CLOSED_WAY_GEOMETRY.osmId,
                            geoPoints = DEFAULT_CLOSED_WAY_GEOMETRY.locations.toGeoPoints(),
                            isClosed = true
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `Given hiking route domain models, when mapHikingRoutes, then correct HikingRoutesItem returns`() {
        val placeName = DEFAULT_HIKING_ROUTE_NAME
        val hikingRoute = HikingRoute(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
        )

        val hikingRouteItems = mapper.mapHikingRoutes(placeName, listOf(hikingRoute))

        assertThat(hikingRouteItems).isEqualTo(
            listOf(
                HikingRoutesItem.Header(placeName),
                HikingRoutesItem.Item(
                    HikingRouteUiModel(
                        osmId = hikingRoute.osmId,
                        name = hikingRoute.name,
                        symbolIcon = hikingRoute.symbolType.getIconRes()
                    )
                )
            )
        )
    }

    @Test
    fun `Given empty hiking route domain models, when mapHikingRoutes, then empty HikingRoutesItem with header returns`() {
        val placeName = DEFAULT_HIKING_ROUTE_NAME

        val hikingRouteItems = mapper.mapHikingRoutes(placeName, emptyList())

        assertThat(hikingRouteItems).isEqualTo(
            listOf(
                HikingRoutesItem.Header(placeName),
                HikingRoutesItem.Empty
            )
        )
    }

    @Test
    fun `Given hiking route details domain model with not relation geometry, when mapHikingRouteDetails, then error throws`() {
        val hikingRoute = HikingRouteUiModel(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolIcon = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL).getIconRes()
        )
        val geometry = Geometry.Node(
            osmId = DEFAULT_PLACE_UI_MODEL.osmId,
            location = DEFAULT_PLACE_UI_MODEL.geoPoint.toLocation()
        )

        assertThrows(IllegalStateException::class.java) {
            placeMapper.mapToHikingRouteDetails(hikingRoute, geometry)
        }
    }

    @Test
    fun `Given hiking route details domain model, when mapHikingRouteDetails, then correct PlaceDetailsUiModel returns`() {
        val hikingRoute = HikingRouteUiModel(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolIcon = SymbolType.PC.getIconRes()
        )
        val geometry = Geometry.Relation(
            osmId = hikingRoute.osmId,
            ways = listOf(
                Geometry.Way(
                    osmId = DEFAULT_WAY_OSM_ID,
                    locations = DEFAULT_WAY_GEOMETRY.map { Location(it.first, it.second) },
                    distance = 100
                ),
                Geometry.Way(
                    osmId = DEFAULT_WAY_OSM_ID,
                    locations = DEFAULT_WAY_GEOMETRY_CLOSED.map { Location(it.first, it.second) },
                    distance = 150
                )
            )
        )

        val placeDetailsUiModel = placeMapper.mapToHikingRouteDetails(hikingRoute, geometry)

        assertThat(placeDetailsUiModel).isEqualTo(
            placeMapper.mapToPlaceDetailsUiModel(
                PlaceUiModel(
                    osmId = hikingRoute.osmId,
                    primaryText = hikingRoute.name.toMessage(),
                    secondaryText = DistanceFormatter.format(100 + 150),
                    placeType = PlaceType.HIKING_ROUTE,
                    iconRes = hikingRoute.symbolIcon,
                    geoPoint = geometry.ways.flatMap { it.locations }
                        .calculateCenter()
                        .toGeoPoint(),
                    placeFeature = PlaceFeature.HIKING_ROUTE_WAYPOINT,
                    boundingBox = null,
                ),
                geometry
            )
        )
    }

    companion object {
        private val DEFAULT_CLOSED_WAY_GEOMETRY = Geometry.Way(
            osmId = DEFAULT_WAY_OSM_ID,
            locations = DEFAULT_WAY_GEOMETRY_CLOSED.map { Location(it.first, it.second) },
            distance = (500..1000).random()
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            primaryText = DEFAULT_NODE_NAME.toMessage(),
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.HIKING_ROUTE_WAYPOINT,
            boundingBox = null,
        )
        private val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES.first()
    }

}
