package hu.mostoha.mobile.android.huki.model.generator

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.*
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.testdata.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.util.Message
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import hu.mostoha.mobile.android.huki.util.calculateCenter
import org.junit.Assert.assertThrows
import org.junit.Test
import org.osmdroid.util.GeoPoint
import java.io.File

class HomeUiModelGeneratorTest {

    private val generator = HomeUiModelGenerator()

    @Test
    fun `Given empty place domain models, when generateSearchBarItems, then error item returns`() {
        val places = emptyList<Place>()

        val searchBarPlaceItems = generator.generateSearchBarItems(places)

        assertThat(searchBarPlaceItems).isEqualTo(
            listOf(
                SearchBarItem.Error(
                    messageRes = R.string.search_bar_empty_message.toMessage(),
                    drawableRes = R.drawable.ic_search_bar_empty_result
                )
            )
        )
    }

    @Test
    fun `Given place domain models, when generateSearchBarItems, then correct list of search bar items return`() {
        val places = listOf(DEFAULT_PLACE_WAY)

        val searchBarPlaceItems = generator.generateSearchBarItems(places)

        assertThat(searchBarPlaceItems).isEqualTo(
            listOf(
                SearchBarItem.Place(
                    PlaceUiModel(
                        osmId = DEFAULT_PLACE_WAY.osmId,
                        placeType = PlaceType.WAY,
                        primaryText = DEFAULT_PLACE_WAY.name,
                        secondaryText = Message.Text("${DEFAULT_PLACE_WAY.postCode} ${DEFAULT_PLACE_WAY.city}"),
                        iconRes = R.drawable.ic_home_search_bar_type_way,
                        geoPoint = DEFAULT_PLACE_WAY.location.toGeoPoint(),
                        boundingBox = DEFAULT_PLACE_WAY.boundingBox,
                    )
                )
            )
        )
    }

    @Test
    fun `Given place domain models without city, when generateSearchBarItems, then secondaryText contains the country`() {
        val places = listOf(DEFAULT_PLACE_WAY.copy(city = null))

        val searchBarPlaceItems = generator.generateSearchBarItems(places)

        assertThat(searchBarPlaceItems).isEqualTo(
            listOf(
                SearchBarItem.Place(
                    PlaceUiModel(
                        osmId = DEFAULT_PLACE_WAY.osmId,
                        placeType = PlaceType.WAY,
                        primaryText = DEFAULT_PLACE_WAY.name,
                        secondaryText = Message.Text("${DEFAULT_PLACE_WAY.postCode} ${DEFAULT_PLACE_WAY.country}"),
                        iconRes = R.drawable.ic_home_search_bar_type_way,
                        geoPoint = DEFAULT_PLACE_WAY.location.toGeoPoint(),
                        boundingBox = DEFAULT_PLACE_WAY.boundingBox,
                    )
                )
            )
        )
    }

    @Test
    fun `Given DomainException, when generatePlacesErrorItem, then proper error SearchBarItem returns`() {
        val domainException = DomainException(R.string.error_message_too_many_requests.toMessage())

        val searchBarErrorItem = generator.generatePlacesErrorItem(domainException)

        assertThat(searchBarErrorItem).isEqualTo(
            listOf(
                SearchBarItem.Error(
                    messageRes = domainException.messageRes,
                    drawableRes = R.drawable.ic_search_bar_error
                )
            )
        )
    }

    @Test
    fun `Given node place UI model, when generatePlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val placeUiModel = DEFAULT_PLACE_UI_MODEL

        val placeDetails = generator.generatePlaceDetails(placeUiModel)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = placeUiModel,
                geometryUiModel = GeometryUiModel.Node(placeUiModel.geoPoint)
            )
        )
    }

    @Test
    fun `Given node geometry domain model, when generatePlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = Geometry.Node(
            osmId = DEFAULT_PLACE_UI_MODEL.osmId,
            location = DEFAULT_PLACE_UI_MODEL.geoPoint.toLocation()
        )

        val placeDetails = generator.generatePlaceDetails(DEFAULT_PLACE_UI_MODEL, geometry)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = DEFAULT_PLACE_UI_MODEL,
                geometryUiModel = GeometryUiModel.Node(DEFAULT_PLACE_UI_MODEL.geoPoint)
            )
        )
    }

    @Test
    fun `Given way geometry domain model, when generatePlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_OPEN_WAY_GEOMETRY

        val placeDetails = generator.generatePlaceDetails(DEFAULT_PLACE_UI_MODEL, geometry)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = DEFAULT_PLACE_UI_MODEL,
                geometryUiModel = GeometryUiModel.Way(
                    osmId = geometry.osmId,
                    geoPoints = geometry.locations.map { it.toGeoPoint() },
                    isClosed = false
                )
            )
        )
    }

    @Test
    fun `Given closed way geometry domain model, when generatePlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_CLOSED_WAY_GEOMETRY

        val placeDetails = generator.generatePlaceDetails(DEFAULT_PLACE_UI_MODEL, geometry)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = DEFAULT_PLACE_UI_MODEL,
                geometryUiModel = GeometryUiModel.Way(
                    osmId = geometry.osmId,
                    geoPoints = geometry.locations.map { it.toGeoPoint() },
                    isClosed = true
                )
            )
        )
    }

    @Test
    fun `Given relation geometry domain model, when generatePlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_CLOSED_RELATION_GEOMETRY

        val placeDetails = generator.generatePlaceDetails(DEFAULT_PLACE_UI_MODEL, geometry)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = DEFAULT_PLACE_UI_MODEL,
                geometryUiModel = GeometryUiModel.Relation(
                    ways = geometry.ways.map { way ->
                        GeometryUiModel.Way(
                            osmId = geometry.osmId,
                            geoPoints = way.locations.map { it.toGeoPoint() },
                            isClosed = way.locations.first() == way.locations.last()
                        )
                    }
                )
            )
        )
    }

    @Test
    fun `Given landscape domain models, when generateLandscapes, then correct PlaceDetailsUiModel returns`() {
        val landscape = DEFAULT_LANDSCAPE

        val places = generator.generateLandscapes(listOf(landscape))

        assertThat(places).isEqualTo(
            listOf(
                PlaceUiModel(
                    osmId = landscape.osmId,
                    placeType = PlaceType.WAY,
                    primaryText = landscape.name,
                    secondaryText = R.string.home_bottom_sheet_landscape_secondary.toMessage(),
                    iconRes = R.drawable.ic_landscapes_mountain_low,
                    geoPoint = landscape.center.toGeoPoint(),
                    boundingBox = null
                )
            )
        )
    }

    @Test
    fun `Given hiking route domain models, when generateHikingRoutes, then correct HikingRoutesItem returns`() {
        val placeName = DEFAULT_HIKING_ROUTE_NAME
        val hikingRoute = HikingRoute(
            osmId = DEFAULT_HIKING_ROUTE_OSM_ID,
            name = DEFAULT_HIKING_ROUTE_NAME,
            symbolType = SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL)
        )

        val hikingRouteItems = generator.generateHikingRoutes(placeName, listOf(hikingRoute))

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
    fun `Given hiking route details domain model with not relation geometry, when generateHikingRouteDetails, then error throws`() {
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
            generator.generateHikingRouteDetails(hikingRoute, geometry)
        }
    }

    @Test
    fun `Given hiking route details domain model, when generateHikingRouteDetails, then correct PlaceDetailsUiModel returns`() {
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

        val placeDetailsUiModel = generator.generateHikingRouteDetails(hikingRoute, geometry)

        assertThat(placeDetailsUiModel).isEqualTo(
            generator.generatePlaceDetails(
                PlaceUiModel(
                    osmId = hikingRoute.osmId,
                    primaryText = hikingRoute.name,
                    secondaryText = DistanceFormatter.format(100 + 150),
                    placeType = PlaceType.RELATION,
                    iconRes = hikingRoute.symbolIcon,
                    geoPoint = geometry.ways.flatMap { it.locations }
                        .calculateCenter()
                        .toGeoPoint(),
                    boundingBox = null
                ),
                geometry
            )
        )
    }

    @Test
    fun `Given null hiking layer file, when generateHikingLayerDetails, then NotDownloaded state returns`() {
        val hikingLayerFile = null

        val placeDetailsUiModel = generator.generateHikingLayerState(hikingLayerFile)

        assertThat(placeDetailsUiModel).isEqualTo(HikingLayerUiModel.NotDownloaded)
    }

    @Test
    fun `Given hiking layer file, when generateHikingLayerDetails, then Downloaded state returns`() {
        val hikingLayerFile = File("")

        val placeDetailsUiModel = generator.generateHikingLayerState(hikingLayerFile)

        assertThat(placeDetailsUiModel).isEqualTo(
            HikingLayerUiModel.Downloaded(
                hikingLayerFile = hikingLayerFile,
                lastUpdatedText = "Jan 1, 1970"
            )
        )
    }

    companion object {
        private val DEFAULT_PLACE_WAY = Place(
            osmId = DEFAULT_WAY_OSM_ID,
            name = DEFAULT_WAY_NAME,
            placeType = PlaceType.WAY,
            location = Location(DEFAULT_WAY_LATITUDE, DEFAULT_WAY_LONGITUDE),
            boundingBox = BoundingBox(
                north = DEFAULT_WAY_EXTENT_NORTH,
                east = DEFAULT_WAY_EXTENT_EAST,
                south = DEFAULT_WAY_EXTENT_SOUTH,
                west = DEFAULT_WAY_EXTENT_WEST
            ),
            country = DEFAULT_WAY_COUNTRY,
            postCode = DEFAULT_WAY_POST_CODE,
            city = DEFAULT_WAY_CITY,
            street = null
        )
        private val DEFAULT_OPEN_WAY_GEOMETRY = Geometry.Way(
            osmId = DEFAULT_WAY_OSM_ID,
            locations = DEFAULT_WAY_GEOMETRY.map { Location(it.first, it.second) },
            distance = (500..1000).random()
        )
        private val DEFAULT_CLOSED_WAY_GEOMETRY = Geometry.Way(
            osmId = DEFAULT_WAY_OSM_ID,
            locations = DEFAULT_WAY_GEOMETRY_CLOSED.map { Location(it.first, it.second) },
            distance = (500..1000).random()
        )
        private val DEFAULT_CLOSED_RELATION_GEOMETRY = Geometry.Relation(
            osmId = DEFAULT_RELATION_OSM_ID,
            ways = DEFAULT_RELATION_GEOMETRY.map { osmIdToGeometry ->
                Geometry.Way(
                    osmId = osmIdToGeometry.first,
                    locations = osmIdToGeometry.second.map { Location(it.first, it.second) },
                    distance = (500..1000).random()
                )
            }
        )
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            primaryText = DEFAULT_NODE_NAME,
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            boundingBox = null
        )
        private val DEFAULT_LANDSCAPE = Landscape(
            osmId = DEFAULT_LANDSCAPE_OSM_ID,
            name = DEFAULT_LANDSCAPE_NAME,
            type = LandscapeType.MOUNTAIN_RANGE_LOW,
            center = Location(DEFAULT_LANDSCAPE_LATITUDE, DEFAULT_LANDSCAPE_LONGITUDE)
        )
    }

}
