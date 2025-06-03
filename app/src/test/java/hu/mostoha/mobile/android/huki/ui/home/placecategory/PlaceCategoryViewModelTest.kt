package hu.mostoha.mobile.android.huki.ui.home.placecategory

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.data.LOCAL_LANDSCAPES
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.center
import hu.mostoha.mobile.android.huki.model.mapper.HikingRouteRelationMapper
import hu.mostoha.mobile.android.huki.model.mapper.HomeUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceAreaMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceCategoryUiModel
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_AREA_BOX
import hu.mostoha.mobile.android.huki.util.DEFAULT_PLACE_PROFILE
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PlaceCategoryViewModelTest {

    private lateinit var viewModel: PlaceCategoryViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val geocodingRepository = mockk<GeocodingRepository>()
    private val homeUiModelMapper = HomeUiModelMapper(PlaceDomainUiMapper(HikingRouteRelationMapper()))
    private val landscapeInteractor = mockk<LandscapeInteractor>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        mockLandscapes()

        viewModel = PlaceCategoryViewModel(
            exceptionLogger,
            geocodingRepository,
            landscapeInteractor,
            homeUiModelMapper
        )
    }

    @Test
    fun `Given bounding box, when init, then place category UI model is emitted`() {
        runTestDefault {
            val boundingBox = DEFAULT_PLACE_AREA_BOX
            val location = boundingBox.center()
            val landscapes = listOf(DEFAULT_LANDSCAPE)

            coEvery { geocodingRepository.getPlaceProfile(location) } returns DEFAULT_PLACE_PROFILE
            coEvery { landscapeInteractor.requestGetLandscapesFlow(any()) } returns flowOf(landscapes)

            viewModel.init(boundingBox)

            viewModel.placeCategoryUiModel.test {
                assertThat(awaitItem()).isEqualTo(PlaceCategoryUiModel())
                assertThat(awaitItem()).isEqualTo(
                    PlaceCategoryUiModel(
                        isAreaLoading = false,
                        placeArea = PlaceAreaMapper.map(location, boundingBox, DEFAULT_PLACE_PROFILE),
                        landscapes = homeUiModelMapper.mapLandscapes(landscapes)
                    )
                )
            }
        }
    }

    private fun mockLandscapes() {
        val landscapes = listOf(DEFAULT_LANDSCAPE)

        every { landscapeInteractor.requestGetLandscapesFlow() } returns flowOf(landscapes)
    }

    companion object {
        private val DEFAULT_LANDSCAPE = LOCAL_LANDSCAPES.first()
    }

}
