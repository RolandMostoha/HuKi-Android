package hu.mostoha.mobile.android.turistautak.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.model.domain.PlaceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
class GooglePlacesRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: GooglePlacesRepository

    @Test
    fun getPlacesBy() = runBlocking {
        val result = repository.getPlacesBy("Dobogókő")

        assertNotNull(result)
    }

    @Test
    fun getPlaceDetails() = runBlocking {
        val result = repository.getPlaceDetails("ChIJg6-_p3F8akcRsON1hSvEAAo", PlaceType.NODE)

        assertNotNull(result)
    }

}