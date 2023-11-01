package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_RELATION_OSM_ID
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_OSM_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@Ignore(
    "Internet connection is unreliable in Firebase Test Lab emulators" +
        "Use this test to ensure Overpass and Photon service availability"
)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class OsmPlacesRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: OsmPlacesRepository

    @Test
    fun givenNodeOsmId_whenGetGeometry_thenLocationIsPresent() = runTest {
        val osmId = DEFAULT_NODE_OSM_ID

        val geometry = repository.getGeometry(osmId, PlaceType.NODE)
        val node = geometry as Geometry.Node

        assertThat(node.osmId).isEqualTo(osmId)
        assertThat(node.location).isEqualTo(Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE))
    }

    @Test
    fun givenWayOsmId_whenGetGeometry_thenLocationListIsNotEmpty() = runTest {
        val osmId = DEFAULT_WAY_OSM_ID

        val geometry = repository.getGeometry(osmId, PlaceType.WAY)
        val way = geometry as Geometry.Way

        assertThat(way.osmId).isEqualTo(osmId)
        assertThat(way.locations).isNotEmpty()
    }

    @Test
    fun givenRelationOsmId_whenGetGeometry_thenWaysListIsNotEmpty() = runTest {
        val osmId = DEFAULT_RELATION_OSM_ID

        val geometry = repository.getGeometry(osmId, PlaceType.RELATION)
        val relation = geometry as Geometry.Relation

        assertThat(relation.osmId).isEqualTo(osmId)
        assertThat(relation.ways).isNotEmpty()
    }

    @Test
    fun givenBoundingBox_whenGetHikingRoutes_thenHikingRouteListIsNotEmpty() = runTest {
        val hikingRoutes = repository.getHikingRoutes(DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES)

        assertThat(hikingRoutes).isNotEmpty()
    }

    companion object {
        private val DEFAULT_BOUNDING_BOX_FOR_HIKING_ROUTES = BoundingBox(
            north = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_NORTH,
            east = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_EAST,
            south = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_SOUTH,
            west = DEFAULT_HIKING_ROUTE_BOUNDING_BOX_WEST
        )
    }

}
