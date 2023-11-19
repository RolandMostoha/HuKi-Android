package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.extensions.toMillis
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_ACTUAL_DATE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_CITY
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LATITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_LONGITUDE
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_NAME
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_NODE_OSM_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import java.util.UUID
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class PlaceHistoryRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: PlaceHistoryRepository

    @Inject
    lateinit var appConfiguration: AppConfiguration

    @Test
    fun givenPlacesInHistory_whenGetPlacesBySearchText_thenSearchResultReturns() {
        runTest {
            repository.savePlace(
                DEFAULT_PLACE_UI_MODEL.copy(
                    osmId = UUID.randomUUID().toString(),
                    primaryText = "Dobogókő Szánkópálya".toMessage(),
                ),
                DEFAULT_ACTUAL_DATE.toMillis()
            )
            repository.savePlace(
                DEFAULT_PLACE_UI_MODEL.copy(
                    osmId = UUID.randomUUID().toString(),
                    primaryText = "Őrség túraútvonal kezdőpontja".toMessage(),
                ),
                DEFAULT_ACTUAL_DATE.toMillis()
            )

            val results = repository.getPlacesBy("szanko")

            results.test {
                val placeList = awaitItem()

                assertThat(placeList.size).isEqualTo(1)
                assertThat(placeList.first().name).isEqualTo("Dobogókő Szánkópálya")
            }
        }
    }

    @Test
    fun givenMorePlacesThanMax_whenClearOldPlaces_thenOldPlacesAreDeleted() {
        runTest {
            val maxRowCount = appConfiguration.getPlaceHistoryMaxRowCount()

            repeat(maxRowCount + 5) { index ->
                delay(100)
                repository.savePlace(
                    DEFAULT_PLACE_UI_MODEL.copy(
                        osmId = index.toString(),
                        primaryText = "Dobogókő Szánkópálya".toMessage(),
                    ),
                    DEFAULT_ACTUAL_DATE.toMillis() + index
                )
            }

            val oldPlaces = repository.getPlaces().first()
            assertThat(oldPlaces.size).isEqualTo(maxRowCount + 5)

            repository.clearOldPlaces()

            val newPlaces = repository.getPlaces().first()
            assertThat(newPlaces.size).isEqualTo(maxRowCount)
            assertThat(newPlaces.first().osmId).isEqualTo("14")
            assertThat(newPlaces.last().osmId).isEqualTo("5")
        }
    }

    companion object {
        private val DEFAULT_PLACE_UI_MODEL = PlaceUiModel(
            osmId = DEFAULT_NODE_OSM_ID,
            placeType = PlaceType.NODE,
            primaryText = DEFAULT_NODE_NAME.toMessage(),
            secondaryText = DEFAULT_NODE_CITY.toMessage(),
            iconRes = 0,
            geoPoint = GeoPoint(DEFAULT_NODE_LATITUDE, DEFAULT_NODE_LONGITUDE),
            placeFeature = PlaceFeature.MAP_SEARCH,
            boundingBox = null,
        )
    }

}
