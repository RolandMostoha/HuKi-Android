package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.HikingRoute
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.network.overpass.Element
import hu.mostoha.mobile.android.huki.model.network.overpass.ElementType
import hu.mostoha.mobile.android.huki.model.network.overpass.Geom
import hu.mostoha.mobile.android.huki.model.network.overpass.Member
import hu.mostoha.mobile.android.huki.model.network.overpass.OverpassQueryResponse
import hu.mostoha.mobile.android.huki.model.network.overpass.SymbolType
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_JEL
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_1_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_1_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_2_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_WAY_2_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import hu.mostoha.mobile.android.huki.util.calculateDistance
import org.junit.Assert.assertThrows
import org.junit.Test

class PlaceDetailsNetworkDomainMapperTest {

    private val mapper = PlaceDetailsNetworkDomainMapper()

    @Test
    fun `Given overpass query response, when generateGeometryByNode, then node Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_NODE.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_NODE))

        val geometry = mapper.mapGeometryByNode(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Node(
                osmId = osmId,
                location = Location(DEFAULT_OVERPASS_ELEMENT_NODE.lat!!, DEFAULT_OVERPASS_ELEMENT_NODE.lon!!)
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByNode, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_NODE.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            mapper.mapGeometryByNode(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateGeometryByWay, then way Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_WAY.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_WAY))
        val expectedLocations = mapper.extractLocations(DEFAULT_OVERPASS_ELEMENT_WAY.geometry!!)

        val geometry = mapper.mapGeometryByWay(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Way(
                osmId = osmId,
                locations = expectedLocations,
                distance = expectedLocations.calculateDistance()
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByWay, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_WAY.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            mapper.mapGeometryByWay(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateGeometryByRelation, then relation Geometry returns`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_RELATION.id.toString()
        val photonQueryResponse = OverpassQueryResponse(listOf(DEFAULT_OVERPASS_ELEMENT_RELATION))

        val geometry = mapper.mapGeometryByRelation(photonQueryResponse, osmId)

        assertThat(geometry).isEqualTo(
            Geometry.Relation(
                osmId = osmId,
                ways = DEFAULT_OVERPASS_ELEMENT_RELATION.members!!.map {
                    val locations = mapper.extractLocations(it.geometry!!)

                    Geometry.Way(
                        osmId = it.ref,
                        locations = locations,
                        distance = locations.calculateDistance()
                    )
                }
            )
        )
    }

    @Test
    fun `Given overpass query without elements, when generateGeometryByRelation, then domain exception throws`() {
        val osmId = DEFAULT_OVERPASS_ELEMENT_RELATION.id.toString()
        val photonQueryResponse = OverpassQueryResponse(emptyList())

        val exception = assertThrows(DomainException::class.java) {
            mapper.mapGeometryByRelation(photonQueryResponse, osmId)
        }

        assertThat(exception.messageRes).isEqualTo(R.string.error_message_missing_osm_id.toMessage())
    }

    @Test
    fun `Given overpass query response, when generateHikingRoutes, then HikingRoute list returns`() {
        val photonQueryResponse = OverpassQueryResponse(
            listOf(
                DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE,
                Element(
                    id = 12345L,
                    type = ElementType.RELATION,
                    tags = mapOf(
                        "jel" to SymbolType.entries.first().osmValue
                    )
                )
            )
        )

        val hikingRoutes = mapper.mapHikingRoutes(photonQueryResponse)

        assertThat(hikingRoutes).isEqualTo(
            listOf(
                HikingRoute(
                    osmId = DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.id.toString(),
                    name = DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.tags!!["name"]!!,
                    symbolType = SymbolType.entries.first {
                        it.osmValue == DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE.tags!!["jel"]
                    }
                )
            )
        )
    }

    @Test
    fun `Given overpass geom list, when extractLocations, then Location list returns`() {
        val geometries = listOf(
            Geom(null, null),
            Geom(null, 19.12345),
            Geom(47.12345, null),
            Geom(47.12345, 19.12345)
        )

        val locations = mapper.extractLocations(geometries)

        assertThat(locations).isEqualTo(listOf(Location(47.12345, 19.12345)))
    }

    @Test
    fun `Given overpass place category list, when mapPlacesByCategories, then Place list returns`() {
        val categories = setOf(PlaceCategory.PARKING, PlaceCategory.PUBLIC_TRANSPORT)
        val queryResponse = OverpassQueryResponse(DEFAULT_OVERPASS_CATEGORY_PLACES)

        val places = mapper.mapPlacesByCategories(queryResponse, categories)

        assertThat(places).hasSize(2)
        assertThat(places.all { it.name == "Parkoló".toMessage() || it.name == "Busz".toMessage() }).isTrue()
    }

    companion object {
        private val DEFAULT_OVERPASS_ELEMENT_NODE = Element(
            id = DEFAULT_NODE_OSM_ID.toLong(),
            type = ElementType.NODE,
            lat = DEFAULT_NODE_LATITUDE,
            lon = DEFAULT_NODE_LONGITUDE
        )
        private val DEFAULT_OVERPASS_ELEMENT_WAY = Element(
            id = DEFAULT_WAY_OSM_ID.toLong(),
            type = ElementType.WAY,
            geometry = DEFAULT_WAY_GEOMETRY.map { Geom(it.first, it.second) }
        )
        private val DEFAULT_OVERPASS_ELEMENT_RELATION = Element(
            id = DEFAULT_RELATION_OSM_ID.toLong(),
            type = ElementType.RELATION,
            members = listOf(
                Member(
                    ref = DEFAULT_RELATION_WAY_1_OSM_ID,
                    type = ElementType.WAY,
                    geometry = DEFAULT_RELATION_WAY_1_GEOMETRY.map { Geom(it.first, it.second) }
                ),
                Member(
                    ref = DEFAULT_RELATION_WAY_2_OSM_ID,
                    type = ElementType.WAY,
                    geometry = DEFAULT_RELATION_WAY_2_GEOMETRY.map { Geom(it.first, it.second) }
                )
            )
        )
        private val DEFAULT_OVERPASS_ELEMENT_HIKING_ROUTE = Element(
            id = DEFAULT_HIKING_ROUTE_OSM_ID.toLong(),
            type = ElementType.RELATION,
            tags = mapOf(
                "name" to "Kéktúra",
                "jel" to SymbolType.valueOf(DEFAULT_HIKING_ROUTE_JEL).osmValue
            )
        )
        private val DEFAULT_OVERPASS_CATEGORY_PLACES =
            listOf(
                Element(
                    id = DEFAULT_NODE_OSM_ID.toLong(),
                    type = ElementType.NODE,
                    tags = mapOf(
                        "name" to "Busz",
                        "public_transport" to "unknown/null"
                    ),
                    lat = DEFAULT_NODE_LATITUDE,
                    lon = DEFAULT_NODE_LONGITUDE,
                ),
                Element(
                    id = DEFAULT_NODE_OSM_ID.toLong(),
                    type = ElementType.NODE,
                    tags = mapOf(
                        "name" to "Parkoló",
                        "amenity" to "parking"
                    ),
                    lat = DEFAULT_NODE_LATITUDE,
                    lon = DEFAULT_NODE_LONGITUDE,
                ),
                Element(
                    id = DEFAULT_NODE_OSM_ID.toLong(),
                    type = ElementType.NODE,
                    tags = mapOf(
                        "name" to "unknown/null",
                        "amenity" to "unknown/null"
                    ),
                    lat = DEFAULT_NODE_LATITUDE,
                    lon = DEFAULT_NODE_LONGITUDE,
                )
            )
    }

}
