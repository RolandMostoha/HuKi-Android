package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import org.junit.Test
import org.osmdroid.util.GeoPoint

class PlaceDomainUiMapperTest {

    private val hikingRouteRelationMapper = HikingRouteRelationMapper()
    private val mapper = PlaceDomainUiMapper(hikingRouteRelationMapper)

    @Test
    fun `Given node geometry domain model, when mapPlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = Geometry.Node(
            osmId = DEFAULT_PLACE_UI_MODEL.osmId,
            location = DEFAULT_PLACE_UI_MODEL.geoPoint.toLocation()
        )

        val placeDetails = mapper.mapToPlaceDetailsUiModel(DEFAULT_PLACE_UI_MODEL, geometry)

        assertThat(placeDetails).isEqualTo(
            PlaceDetailsUiModel(
                placeUiModel = DEFAULT_PLACE_UI_MODEL,
                geometryUiModel = GeometryUiModel.Node(DEFAULT_PLACE_UI_MODEL.geoPoint)
            )
        )
    }

    @Test
    fun `Given way geometry domain model, when mapPlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_OPEN_WAY_GEOMETRY

        val placeDetails = mapper.mapToPlaceDetailsUiModel(DEFAULT_PLACE_UI_MODEL, geometry)

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
    fun `Given closed way geometry domain model, when mapPlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_CLOSED_WAY_GEOMETRY

        val placeDetails = mapper.mapToPlaceDetailsUiModel(DEFAULT_PLACE_UI_MODEL, geometry)

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
    fun `Given relation geometry domain model, when mapPlaceDetails, then correct PlaceDetailsUiModel returns`() {
        val geometry = DEFAULT_CLOSED_RELATION_GEOMETRY

        val placeDetails = mapper.mapToPlaceDetailsUiModel(DEFAULT_PLACE_UI_MODEL, geometry)

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

    companion object {
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
            primaryText = DEFAULT_NODE_NAME.toMessage(),
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.HIKING_ROUTE_WAYPOINT,
            boundingBox = null,
        )
    }

}
