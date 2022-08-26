package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.Geometry
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

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
    fun givenSearchText_whenGetPlacesBy_thenResultIsNotNull() = runTest {
        val places = repository.getPlacesBy("Mecsek")

        assertThat(places).isNotEmpty()
    }

    @Test
    fun givenNodeOsmId_whenGetGeometry_thenLocationIsPresent() = runTest {
        val osmId = DEFAULT_NODE_OSM_ID

        val geometry = repository.getGeometry(osmId, PlaceType.NODE)
        val node = geometry as Geometry.Node

        assertThat(node.osmId).isEqualTo(osmId)
        assertThat(node.location).isEqualTo(Location(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE))
    }

}
