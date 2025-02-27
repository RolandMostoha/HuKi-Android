package hu.mostoha.mobile.android.huki.ui.home.history.gpx

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.mapper.HikingRouteRelationMapper
import hu.mostoha.mobile.android.huki.model.mapper.HistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceDomainUiMapper
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.util.DEFAULT_LOCAL_DATE
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.TimberRule
import hu.mostoha.mobile.android.huki.util.answerDefaults
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@ExperimentalCoroutinesApi
class GpxHistoryViewModelTest {

    private lateinit var viewModel: GpxHistoryViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val layersRepository = mockk<LayersRepository>()
    private val dateTimeProvider = mockk<DateTimeProvider>()
    private val mapper = HistoryUiModelMapper(PlaceDomainUiMapper(HikingRouteRelationMapper()))

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        dateTimeProvider.answerDefaults()
        every { DEFAULT_ROUTE_PLANNER_GPX_FILE_URI.lastPathSegment } returns "route_plan_HuKi938.gpx"
        every { DEFAULT_EXTERNAL_GPX_FILE_URI.lastPathSegment } returns "dera_szurdok.gpx"
        coEvery { layersRepository.getGpxHistory() } returns DEFAULT_GPX_HISTORY

        viewModel = GpxHistoryViewModel(
            exceptionLogger,
            layersRepository,
            mapper,
            dateTimeProvider,
            mainCoroutineRule.testDispatcher,
        )
    }

    @Test
    fun `When init, then gpx history file names return GPX file names without file extension`() {
        runTestDefault {
            viewModel.gpxHistoryFileNames.test {
                assertThat(awaitItem()).isEqualTo(listOf("route_plan_HuKi938", "dera_szurdok"))
            }
        }
    }

    @Test
    fun `Given a route planner GPX, when init, then route planner gpx history adapter items are emitted`() {
        runTestDefault {
            viewModel.gpxHistory.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(mapper.mapGpxHistory(DEFAULT_GPX_HISTORY, DEFAULT_LOCAL_DATE))
            }
        }
    }

    @Test
    fun `Given error, when delete GPX, then error message is emitted`() {
        runTestDefault {
            coEvery { layersRepository.deleteGpx(any()) } throws Exception("Error")

            viewModel.errorMessage.test {
                viewModel.deleteGpx(DEFAULT_ROUTE_PLANNER_GPX_FILE_URI)

                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.gpx_history_rename_operation_error))
            }
        }
    }

    @Test
    fun `Given error, when rename GPX, then error message is emitted`() {
        runTestDefault {
            coEvery { layersRepository.renameGpx(any(), any()) } throws Exception("Error")

            viewModel.errorMessage.test {
                viewModel.renameGpx(GpxRenameResult(DEFAULT_ROUTE_PLANNER_GPX_FILE_URI, "new_name"))

                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.gpx_history_rename_operation_error))
            }
        }
    }

    companion object {
        @get:ClassRule
        @JvmStatic
        var timberRule = TimberRule()

        private val DEFAULT_ROUTE_PLANNER_GPX_FILE_URI = mockk<Uri>()
        private val DEFAULT_EXTERNAL_GPX_FILE_URI = mockk<Uri>()
        private val DEFAULT_GPX_HISTORY = GpxHistory(
            routePlannerGpxList = listOf(
                GpxHistoryItem(
                    name = "route_plan_HuKi938.gpx",
                    fileUri = DEFAULT_ROUTE_PLANNER_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                    travelTime = 5.hours,
                    distance = 10000,
                    incline = 1000,
                    decline = 1000,
                    waypointCount = 0,
                )
            ),
            externalGpxList = listOf(
                GpxHistoryItem(
                    name = "dera_szurdok.gpx",
                    fileUri = DEFAULT_EXTERNAL_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 3, 16, 0),
                    travelTime = Duration.ZERO,
                    distance = 0,
                    incline = 0,
                    decline = 0,
                    waypointCount = 100,
                )
            )
        )
    }

}
