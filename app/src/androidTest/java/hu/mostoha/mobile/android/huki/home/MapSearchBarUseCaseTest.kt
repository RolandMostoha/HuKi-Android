package hu.mostoha.mobile.android.huki.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.RepositoryModule
import hu.mostoha.mobile.android.huki.di.module.ServiceModule
import hu.mostoha.mobile.android.huki.extensions.copyFrom
import hu.mostoha.mobile.android.huki.model.domain.PlacePrediction
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import hu.mostoha.mobile.android.huki.ui.home.HomeActivity
import hu.mostoha.mobile.android.huki.util.espresso.isPopupTextDisplayed
import hu.mostoha.mobile.android.huki.util.espresso.typeText
import hu.mostoha.mobile.android.huki.util.launch
import hu.mostoha.mobile.android.huki.util.testContext
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class MapSearchBarUseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val layerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placeRepository: PlacesRepository = mockk()

    @BindValue
    @JvmField
    val landscapeRepository: LandscapeRepository = LocalLandscapeRepository()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun givenSearchText_whenTyping_thenPlacePredictionsDisplay() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction("1", PlaceType.WAY, "Mecseki Kéktúra", null),
            PlacePrediction("2", PlaceType.NODE, "Mecseknádasdi Piroska", "Mecseknádasd")
        )

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            "Mecseki Kéktúra".isPopupTextDisplayed()
            "Mecseknádasdi Piroska".isPopupTextDisplayed()
        }
    }

    @Test
    fun givenEmptyResult_whenTyping_thenEmptyViewDisplay() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } returns emptyList()

        launch<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.search_bar_empty_message.isPopupTextDisplayed()
        }
    }

    @Test
    fun givenErrorResult_whenTyping_thenErrorViewDisplay() {
        answerTestHikingLayer()
        coEvery { placeRepository.getPlacesBy(any()) } throws IllegalStateException()

        launch<HomeActivity> {
            val searchText = "QWER"

            R.id.homeSearchBarInput.typeText(searchText)

            R.string.default_error_message_unknown.isPopupTextDisplayed()
        }
    }

    private fun answerTestHikingLayer() {
        coEvery { layerRepository.getHikingLayerFile() } returns osmConfiguration.getHikingLayerFile().apply {
            copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
    }

}