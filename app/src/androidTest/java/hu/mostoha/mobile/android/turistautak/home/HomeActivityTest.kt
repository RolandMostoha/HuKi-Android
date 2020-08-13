package hu.mostoha.mobile.android.turistautak.home

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.di.module.RepositoryModule
import hu.mostoha.mobile.android.turistautak.di.module.ServiceModule
import hu.mostoha.mobile.android.turistautak.extensions.copyFrom
import hu.mostoha.mobile.android.turistautak.model.domain.*
import hu.mostoha.mobile.android.turistautak.osmdroid.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.repository.HikingLayerRepository
import hu.mostoha.mobile.android.turistautak.repository.PlacesRepository
import hu.mostoha.mobile.android.turistautak.ui.home.HomeActivity
import hu.mostoha.mobile.android.turistautak.util.*
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
@UninstallModules(RepositoryModule::class, ServiceModule::class)
class HomeActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @BindValue
    @JvmField
    val layerRepository: HikingLayerRepository = mockk()

    @BindValue
    @JvmField
    val placeRepository: PlacesRepository = mockk()

    @Before
    fun init() {
        hiltRule.inject()
        osmConfiguration.init()
    }

    @Test
    fun givenNullLayerFile_whenHomeOpens_thenDownloadDialogDisplays() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            R.string.download_layer_dialog_title.isTextDisplayed()
        }
    }

    @Test
    fun givenNullLayerFile_whenDialogCanceled_thenMapShouldShown() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            R.string.download_layer_dialog_negative_button.clickWithText()

            R.id.homeMapView.isDisplayed()
        }
    }

    @Test
    fun givenNullLayerFile_whenMyLocationClicked_thenMyLocationOverlayDisplays() {
        coEvery { layerRepository.getHikingLayerFile() } returns null

        launch<HomeActivity> {
            R.string.download_layer_dialog_negative_button.clickWithText()
            R.id.homeMyLocationButton.click()

            // TODO: Check overlay displays
        }
    }

    @Test
    fun givenTuraReteg1000_whenHomeOpens_thenLayerDisplays() {
        answerTestHikingLayer()

        launch<HomeActivity> {
            // TODO: Check overlay displays
        }
    }

    @Test
    fun givenSearchText_whenTyping_thenPlacePredictionsDisplay() {
        answerTestHikingLayer()
        answerTestPlacePredictions()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)

            "Mecseki Kéktúra".isPopupTextDisplayed()
            "Mecseknádasdi Piroska".isPopupTextDisplayed()
        }
    }

    @Test
    fun givenPlacePrediction_whenClick_thenPlaceDetailsDisplay() {
        answerTestHikingLayer()
        answerTestPlacePredictions()
        answerTestPlaceDetails()

        launch<HomeActivity> {
            val searchText = "Mecsek"

            R.id.homeSearchBarInput.typeText(searchText)
            "Mecseki Kéktúra".clickWithTextInPopup()

            // TODO: Check overlay displays
        }
    }

    private fun answerTestPlacePredictions() {
        coEvery { placeRepository.getPlacesBy(any()) } returns listOf(
            PlacePrediction(UUID.randomUUID().toString(), PlaceType.WAY, "Mecseki Kéktúra", null),
            PlacePrediction(UUID.randomUUID().toString(), PlaceType.NODE, "Mecseknádasdi Piroska", "Mecseknádasd")
        )
    }

    private fun answerTestPlaceDetails() {
        coEvery { placeRepository.getPlaceDetails(any(), any()) } returns PlaceDetails(
            UUID.randomUUID().toString(),
            PayLoad.Node(Location(47.123, 19.123))
        )
    }

    private fun answerTestHikingLayer() {
        val file = osmConfiguration.getHikingLayerFile().also {
            it.copyFrom(testContext.assets.open("TuraReteg_1000.mbtiles"))
        }
        coEvery { layerRepository.getHikingLayerFile() } returns file
    }

}

