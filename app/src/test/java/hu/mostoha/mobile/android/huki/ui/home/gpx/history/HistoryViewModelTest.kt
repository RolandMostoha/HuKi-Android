package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.GpxHistory
import hu.mostoha.mobile.android.huki.model.domain.GpxHistoryItem
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.mapper.GpxHistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class HistoryViewModelTest {

    private lateinit var viewModel: HistoryViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val layersRepository = mockk<LayersRepository>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { DEFAULT_GPX_FILE_URI.lastPathSegment } returns "dera_szurdok.gpx"
        coEvery { layersRepository.getGpxHistory() } returns GpxHistory(
            routePlannerGpxList = listOf(
                GpxHistoryItem(
                    name = "dera_szurdok.gpx",
                    fileUri = DEFAULT_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                )
            ),
            externalGpxList = emptyList()
        )

        viewModel = HistoryViewModel(
            exceptionLogger,
            layersRepository,
            GpxHistoryUiModelMapper(),
            mainCoroutineRule.testDispatcher,
        )
    }

    @Test
    fun `When init, then gpx history file names return GPX file names without file extension`() {
        runTestDefault {
            viewModel.gpxHistoryFileNames.test {
                assertThat(awaitItem()).isEqualTo(emptyList<String>())
                assertThat(awaitItem()).isEqualTo(listOf("dera_szurdok"))
            }
        }
    }

    @Test
    fun `Given a route planner GPX, when init, then route planner gpx history adapter items are emitted`() {
        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(DEFAULT_ROUTE_PLANNER_GPX_HISTORY_ITEM))
            }
        }
    }

    @Test
    fun `Given an external GPX, when init, then route planner gpx history adapter items are emitted`() {
        coEvery { layersRepository.getGpxHistory() } returns GpxHistory(
            routePlannerGpxList = emptyList(),
            externalGpxList = listOf(
                GpxHistoryItem(
                    name = "dera_szurdok.gpx",
                    fileUri = DEFAULT_GPX_FILE_URI,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                )
            ),
        )

        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                viewModel.tabSelected(HistoryTab.EXTERNAL)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        GpxHistoryAdapterModel.InfoView(
                            message = R.string.gpx_history_item_route_planner_empty,
                            iconRes = R.drawable.ic_gpx_history_empty
                        )
                    )
                )
                assertThat(awaitItem()).isEqualTo(listOf(DEFAULT_EXTERNAL_GPX_HISTORY_ITEM))
            }
        }
    }

    @Test
    fun `When select external tab, then empty external items are emitted`() {
        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                viewModel.tabSelected(HistoryTab.EXTERNAL)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(listOf(DEFAULT_ROUTE_PLANNER_GPX_HISTORY_ITEM))
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        GpxHistoryAdapterModel.InfoView(
                            message = R.string.gpx_history_item_external_empty,
                            iconRes = R.drawable.ic_gpx_history_empty
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `Given error, when delete GPX, then error message is emitted`() {
        runTestDefault {
            coEvery { layersRepository.deleteGpx(any()) } throws Exception("Error")

            viewModel.errorMessage.test {
                viewModel.deleteGpx(DEFAULT_GPX_FILE_URI)

                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.gpx_history_rename_operation_error))
            }
        }
    }

    @Test
    fun `Given error, when rename GPX, then error message is emitted`() {
        runTestDefault {
            coEvery { layersRepository.renameGpx(any(), any()) } throws Exception("Error")

            viewModel.errorMessage.test {
                viewModel.renameGpx(GpxRenameResult(DEFAULT_GPX_FILE_URI, "new_name"))

                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.gpx_history_rename_operation_error))
            }
        }
    }

    companion object {
        private val DEFAULT_GPX_FILE_URI = mockk<Uri>()
        private val DEFAULT_ROUTE_PLANNER_GPX_HISTORY_ITEM = GpxHistoryAdapterModel.Item(
            name = "dera_szurdok.gpx",
            gpxType = GpxType.ROUTE_PLANNER,
            fileUri = DEFAULT_GPX_FILE_URI,
            dateText = Message.Res(
                R.string.gpx_history_item_route_planner_date_template,
                listOf("2023.06.02 16:00")
            ),
        )
        private val DEFAULT_EXTERNAL_GPX_HISTORY_ITEM = GpxHistoryAdapterModel.Item(
            name = "dera_szurdok.gpx",
            gpxType = GpxType.EXTERNAL,
            fileUri = DEFAULT_GPX_FILE_URI,
            dateText = Message.Res(
                R.string.gpx_history_item_external_date_template,
                listOf("2023.06.02 16:00")
            ),
        )
    }

}
