package hu.mostoha.mobile.android.huki.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class DefaultLayersRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var repository: DefaultLayersRepository

    @Inject
    lateinit var gpxConfiguration: GpxConfiguration

    @Test
    fun givenTileZoomRangesRawResource_whenGetHikingLayerZoomRanges_thenCorrectTileZoomRangeListReturns() =
        runTest {
            val zoomRanges = repository.getHikingLayerZoomRanges()

            assertThat(zoomRanges).isNotEmpty()
        }

}
