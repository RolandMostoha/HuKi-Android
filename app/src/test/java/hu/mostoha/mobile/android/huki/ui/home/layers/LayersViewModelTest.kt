package hu.mostoha.mobile.android.huki.ui.home.layers

import android.net.Uri
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.LayersInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.LayersConfig
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.mapper.LayersUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_GPX_WAY_CLOSED
import hu.mostoha.mobile.android.huki.testdata.DEFAULT_WAY_GEOMETRY_CLOSED
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.calculateTravelTime
import hu.mostoha.mobile.android.huki.util.flowOfError
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LayersViewModelTest {

    private lateinit var viewModel: LayersViewModel

    private val exceptionLogger = mockk<ExceptionLogger>()

    private val layersInteractor = mockk<LayersInteractor>()

    private val tileUrlProvider = mockk<HikingTileUrlProvider>()

    private val gpxFileUri = mockk<Uri>()

    private val layersUiModelMapper = LayersUiModelMapper()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { layersInteractor.requestHikingLayerZoomRanges() } returns flowOf(emptyList())

        viewModel = LayersViewModel(
            mainCoroutineRule.testDispatcher,
            exceptionLogger,
            tileUrlProvider,
            layersInteractor,
            layersUiModelMapper
        )
    }

    @Test
    fun `When init, then layers config is emitted with null and then with the configured hiking layer`() =
        runTestDefault {
            viewModel.layersConfig.test {
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.Mapnik,
                        hikingLayer = null
                    )
                )
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.Mapnik,
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
    fun `Given new base layer, when selectLayer, then layers config is updated`() =
        runTestDefault {
            viewModel.layersConfig.test {
                advanceUntilIdle()

                viewModel.selectLayer(LayerType.OPEN_TOPO)

                skipItems(2)
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.OpenTopo,
                        hikingLayer = HikingLayer(
                            LayerType.HUNGARIAN_HIKING_LAYER,
                            AwsHikingTileSource(tileUrlProvider, emptyList())
                        )
                    )
                )
            }
        }

    @Test
    fun `When deselect hiking layer, then layers config is updated with null hiking layer`() =
        runTestDefault {
            viewModel.layersConfig.test {
                advanceUntilIdle()

                viewModel.selectLayer(LayerType.HUNGARIAN_HIKING_LAYER)

                skipItems(2)
                assertThat(awaitItem()).isEqualTo(
                    LayersConfig(
                        baseLayer = BaseLayer.Mapnik,
                        hikingLayer = null
                    )
                )
            }
        }

    @Test
    fun `When select Open Topo and deselect Hiking layer, then layer adapter items are updated`() =
        runTestDefault {
            viewModel.layerAdapterItems.test {
                advanceUntilIdle()

                viewModel.selectLayer(LayerType.OPEN_TOPO)
                viewModel.selectLayer(LayerType.HUNGARIAN_HIKING_LAYER)

                skipItems(3)

                assertThat(awaitItem()).isEqualTo(createSelectedAdapterItems(listOf(LayerType.OPEN_TOPO)))
            }
        }

    @Test
    fun `Given loaded GPX, when deselect GPX layer, then layer adapter items are updated`() =
        runTestDefault {
            every { layersInteractor.requestGpxDetails(gpxFileUri) } returns flowOf(DEFAULT_GPX_DETAILS)

            viewModel.layerAdapterItems.test {
                advanceUntilIdle()

                viewModel.loadGpx(gpxFileUri)
                viewModel.selectLayer(LayerType.GPX)

                skipItems(3)

                assertThat(awaitItem()).isEqualTo(
                    createSelectedAdapterItems(
                        listOf(LayerType.MAPNIK, LayerType.HUNGARIAN_HIKING_LAYER, LayerType.GPX)
                    )
                )
                assertThat(awaitItem()).isEqualTo(
                    createSelectedAdapterItems(
                        listOf(LayerType.MAPNIK, LayerType.HUNGARIAN_HIKING_LAYER)
                    )
                )
            }
        }

    @Test
    fun `Given file URI, when loadGpx, then gpx details UI model is emitted`() =
        runTestDefault {
            every { layersInteractor.requestGpxDetails(gpxFileUri) } returns flowOf(DEFAULT_GPX_DETAILS)

            viewModel.loadGpx(gpxFileUri)

            viewModel.gpxDetailsUiModel.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(layersUiModelMapper.mapGpxDetails(DEFAULT_GPX_DETAILS))
            }
        }

    @Test
    fun `Given error during request gpx, when loadGpx, then error message is emitted`() =
        runTestDefault {
            every {
                layersInteractor.requestGpxDetails(gpxFileUri)
            } returns flowOfError(GpxParseFailedException(IllegalStateException("")))

            viewModel.loadGpx(gpxFileUri)

            viewModel.errorMessage.test {
                assertThat(awaitItem()).isNull()
                assertThat(awaitItem()).isEqualTo(Message.Res(R.string.error_message_gpx_parse_failed))
            }
        }

    @Test
    fun `When clear error, then null error message is emitted`() =
        runTestDefault {
            every {
                layersInteractor.requestGpxDetails(null)
            } returns flowOfError(GpxParseFailedException(IllegalStateException("")))

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
            LayersAdapterItem.Header(R.string.layers_base_layers_header),
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
            LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
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
            locations = DEFAULT_WAY_GEOMETRY_CLOSED.map { Location(it.first, it.second) },
            travelTime = DEFAULT_GPX_WAY_CLOSED
                .map { Location(it.first, it.second) }
                .calculateTravelTime(),
            distance = 15000,
            altitudeRange = 300 to 800,
            incline = 500,
            decline = 300,
            isClosed = true
        )
    }

}
