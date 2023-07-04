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
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class GpxHistoryViewModelTest {

    private lateinit var viewModel: GpxHistoryViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val layersRepository = mockk<LayersRepository>()
    private val gpxFileUri = mockk<Uri>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        coEvery { layersRepository.getGpxHistory() } returns GpxHistory(
            routePlannerGpxList = listOf(
                GpxHistoryItem(
                    name = "dera_szurdok.gpx",
                    fileUri = gpxFileUri,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                )
            ),
            externalGpxList = emptyList()
        )

        viewModel = GpxHistoryViewModel(
            exceptionLogger,
            layersRepository,
            GpxHistoryUiModelMapper(),
        )
    }

    @Test
    fun `Given a route planner GPX, when init, then route planner gpx history adapter items are emitted`() {
        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        GpxHistoryAdapterModel.Item(
                            name = "dera_szurdok.gpx",
                            gpxType = GpxType.ROUTE_PLANNER,
                            fileUri = gpxFileUri,
                            dateText = Message.Res(
                                R.string.gpx_history_item_route_planner_date_template,
                                listOf("2023.06.02 16:00")
                            ),
                        )
                    )
                )
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
                    fileUri = gpxFileUri,
                    lastModified = LocalDateTime.of(2023, 6, 2, 16, 0),
                )
            ),
        )

        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                viewModel.tabSelected(GpxHistoryTab.EXTERNAL)

                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        GpxHistoryAdapterModel.Item(
                            name = "dera_szurdok.gpx",
                            gpxType = GpxType.EXTERNAL,
                            fileUri = gpxFileUri,
                            dateText = Message.Res(
                                R.string.gpx_history_item_external_date_template,
                                listOf("2023.06.02 16:00")
                            ),
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `When select external tab, then empty external items are emitted`() {
        runTestDefault {
            viewModel.gpxHistoryAdapterItems.test {
                viewModel.tabSelected(GpxHistoryTab.EXTERNAL)

                assertThat(awaitItem()).isNull()
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

}
