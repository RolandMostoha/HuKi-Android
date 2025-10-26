package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.util.HUNGARY_BOUNDING_BOX
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
class PhotonGeocodingRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: PhotonGeocodingRepository

    @Test
    fun givenQuery_whenAutocomplete_thenPlacesReturn() = runTest {
        val boundingBox = HUNGARY_BOUNDING_BOX
        val searchText = "Bél-kő"

        val places = repository.getAutocompletePlaces(searchText, boundingBox)

        assertThat(places).isNotEmpty()
    }

}