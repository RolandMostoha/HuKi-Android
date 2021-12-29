package hu.mostoha.mobile.android.huki.home

import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.ServiceModule
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.hasCenterAndZoom
import hu.mostoha.mobile.android.huki.util.launch
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapWithoutGpsUseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val hikingLayerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placesRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun givenNullHikingLayer_whenMapOpens_thenItIsCenteredAndZoomedToHungary() {
        answerNullHikingLayer()

        launch<HomeActivity> {
            Espresso.pressBack()

            R.id.homeMapView.hasCenterAndZoom(
                center = HUNGARY_BOUNDING_BOX_CENTER,
                zoom = HUNGARY_BOUNDING_BOX_ZOOM
            )
        }
    }

    companion object {
        private const val HUNGARY_BOUNDING_BOX_ZOOM = 7.227610802851414
        private val HUNGARY_BOUNDING_BOX_CENTER = GeoPoint(47.31885723983627, 19.45407265979361)
    }

    private fun answerNullHikingLayer() {
        coEvery { hikingLayerRepository.getHikingLayerFile() } returns null
    }

}
