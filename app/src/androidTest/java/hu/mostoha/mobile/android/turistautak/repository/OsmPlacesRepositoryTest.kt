package hu.mostoha.mobile.android.turistautak.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.model.domain.BoundingBox
import hu.mostoha.mobile.android.turistautak.model.domain.Location
import hu.mostoha.mobile.android.turistautak.model.domain.PayLoad
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
@UninstallModules(ServiceModule::class)
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
    fun givenSearchText_whenGetPlacesBy_thenResultIsNotNull() = runBlocking {
        val result = repository.getPlacesBy("Dobogókő")

        assertNotNull(result)
    }

    @Test
    fun givenNodeType_whenGetPlaceDetails_thenResultIsNotNull() = runBlocking {
        val placeId = "130074457"

        val result = repository.getPlaceDetails(placeId, PlaceType.NODE)

        assertEquals(placeId, result.id)
        assertEquals(Location(47.7193842, 18.8962014), (result.payLoad as PayLoad.Node).location)
    }

    @Test
    fun givenWayType_whenGetPlaceDetails_thenResultIsNotNull() = runBlocking {
        val result = repository.getPlaceDetails("671340353", PlaceType.WAY)

        assertNotNull(result)
    }

    @Test
    fun givenRelationType_whenGetPlaceDetails_thenResultIsNotNull() = runBlocking {
        val result = repository.getPlaceDetails("382123", PlaceType.RELATION)

        assertNotNull(result)
    }

    @Test
    fun givenBoundingBox_whenGetHikingRoutes_thenResultIsNotNull() = runBlocking {
        val result = repository.getHikingRoutes(BoundingBox(48.0058358, 20.2007937, 47.7747357, 19.6898570))

        assertNotNull(result)
    }

}