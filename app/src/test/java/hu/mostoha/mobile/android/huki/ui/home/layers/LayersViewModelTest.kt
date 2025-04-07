package hu.mostoha.mobile.android.huki.ui.home.layers

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.LayersConfig
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toGpxWaypointsByLocations
import hu.mostoha.mobile.android.huki.model.domain.toLocationsTriple
import hu.mostoha.mobile.android.huki.model.mapper.LayersUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LayersViewModelTest {

    private lateinit var viewModel: LayersViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()
    private val layersRepository = mockk<LayersRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val tileUrlProvider = mockk<HikingTileUrlProvider>()
    private val gpxFileUri = mockk<Uri>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)
    private val layersUiModelMapper = LayersUiModelMapper()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        coEvery { layersRepository.getHikingLayerZoomRanges() } returns emptyList()
        coEvery { settingsRepository.saveBaseLayer(any()) } returns Unit
        every { settingsRepository.getBaseLayer() } returns flowOf(BaseLayer.MAPNIK)

        viewModel = LayersViewModel(
            SavedStateHandle(),
            UnconfinedTestDispatcher(),
            layersRepository,
            settingsRepository,
            layersUiModelMapper,
            tileUrlProvider,
            exceptionLogger,
            analyticsService,
        )
    }

    @Test
    fun `When init, then layers config is emitted with null and then with the configured hiking layer`() =
        runTestDefault {
            viewModel.layersConfig.test {
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.MAPNIK,
                        hikingLayer = null
                    )
                )
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.MAPNIK,
                        hikingLayer = HikingLayer(
                            LayerType.HUNGARIAN_HIKING_LAYER,
                            AwsHikingTileSource(tileUrlProvider, emptyList())
                        )
                    )
                )
            }
        }

    @Test
    fun `When init, then layer adapter items are emitted with unselected and then selected hiking layer`() =
        runTestDefault {
            viewModel.layerAdapterItems.test {
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(emptyList<LayersAdapterItem>())
                assertThat(awaitItem()).isEqualTo(createSelectedAdapterItems(listOf(LayerType.MAPNIK)))
                assertThat(awaitItem()).isEqualTo(
                    createSelectedAdapterItems(listOf(LayerType.MAPNIK, LayerType.HUNGARIAN_HIKING_LAYER))
                )
            }
        }

    @Test
    fun `Given new base layer, when selectLayer, then layer is saved in repository`() {
        viewModel.selectBaseLayer(BaseLayer.OPEN_TOPO)

        coVerify {
            settingsRepository.saveBaseLayer(BaseLayer.OPEN_TOPO)
        }
    }

    @Test
    fun `When deselect hiking layer, then layers config is updated with null hiking layer`() =
        runTestDefault {
            viewModel.layersConfig.test {
                advanceUntilIdle()

                viewModel.selectHikingLayer()

                skipItems(2)
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.MAPNIK,
                        hikingLayer = null
                    )
                )
            }
        }

    @Test
    fun `Given file URI, when loadGpx, then gpx details UI model is emitted`() =
        runTestDefault {
            coEvery { layersRepository.getGpxDetails(gpxFileUri) } returns DEFAULT_GPX_DETAILS
            coEvery { settingsRepository.isGpxSlopeColoringEnabled() } returns flowOf(true)

            viewModel.loadGpx(gpxFileUri)

            viewModel.gpxDetailsUiModel.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(layersUiModelMapper.mapGpxDetails(DEFAULT_GPX_DETAILS, true))
            }
        }

    @Test
    fun `Given error during request gpx, when loadGpx, then error message is emitted`() =
        runTestDefault {
            coEvery {
                layersRepository.getGpxDetails(gpxFileUri)
            } throws GpxParseFailedException(IllegalStateException(""))

            viewModel.loadGpx(gpxFileUri)

            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.error_message_gpx_parse_failed))
            }
        }

    @Test
    fun `When clear error, then null error message is emitted`() =
        runTestDefault {
            coEvery {
                layersRepository.getGpxDetails(null)
            } throws GpxParseFailedException(IllegalStateException(""))

            viewModel.loadGpx(null)

            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.error_message_gpx_parse_failed))

                viewModel.clearError()

                assertThat(awaitItem()).isNull()
            }
        }

    private fun createSelectedAdapterItems(selectedLayerTypes: List<LayerType>): List<LayersAdapterItem> {
        return listOf(
            LayersAdapterItem.Header(
                titleRes = R.string.layers_base_layers_header,
                isCloseVisible = true
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.MAPNIK,
                titleRes = R.string.layers_mapnik_title,
                drawableRes = R.drawable.ic_layers_mapnik,
                isSelected = selectedLayerTypes.contains(LayerType.MAPNIK)
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.OPEN_TOPO,
                titleRes = R.string.layers_open_topo_title,
                drawableRes = R.drawable.ic_layers_open_topo,
                isSelected = selectedLayerTypes.contains(LayerType.OPEN_TOPO)
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.TUHU,
                titleRes = R.string.layers_tuhu_title,
                drawableRes = R.drawable.ic_layers_tuhu,
                isSelected = selectedLayerTypes.contains(LayerType.TUHU)
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.GOOGLE_SATELLITE,
                titleRes = R.string.layers_google_satellite_title,
                drawableRes = R.drawable.ic_layers_google_satellite,
                isSelected = selectedLayerTypes.contains(LayerType.GOOGLE_SATELLITE)
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.MERRETEKERJEK,
                titleRes = R.string.layers_merretekerjek_title,
                drawableRes = R.drawable.ic_layers_merretekerjek,
                isSelected = selectedLayerTypes.contains(LayerType.MERRETEKERJEK)
            ),
            LayersAdapterItem.Header(
                titleRes = R.string.layers_hiking_layers_header,
                isCloseVisible = false
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.HUNGARIAN_HIKING_LAYER,
                titleRes = R.string.layers_hiking_hungarian_title,
                drawableRes = R.drawable.ic_layers_hiking,
                isSelected = selectedLayerTypes.contains(LayerType.HUNGARIAN_HIKING_LAYER)
            ),
            LayersAdapterItem.Layer(
                layerType = LayerType.GPX,
                titleRes = R.string.layers_gpx_title,
                drawableRes = R.drawable.ic_layers_gpx,
                isSelected = selectedLayerTypes.contains(LayerType.GPX)
            )
        )
    }

    companion object {
        private val DEFAULT_GPX_DETAILS = GpxDetails(
            fileName = "dera_szurdok.gpx",
            fileUri = "file://dera_szurdok.gpx",
            locations = DEFAULT_GPX_WAY_CLOSED.map { Location(it.first, it.second, it.third) },
            gpxWaypoints = DEFAULT_GPX_WAY_CLOSED.toLocationsTriple().toGpxWaypointsByLocations(),
            travelTime = DEFAULT_GPX_WAY_CLOSED
                .map { Location(it.first, it.second) }
                .calculateTravelTime(),
            distance = 15000,
            altitudeRange = 300 to 800,
            incline = 500,
            decline = 300,
        )
    }

}
