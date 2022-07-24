package hu.mostoha.mobile.android.huki.ui.home.layers

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.HikingLayerInteractor
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.LayersConfig
import hu.mostoha.mobile.android.huki.osmdroid.tiles.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tiles.HikingTileUrlProvider
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
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

    private val hikingLayerInteractor = mockk<HikingLayerInteractor>()

    private val tileUrlProvider = mockk<HikingTileUrlProvider>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { hikingLayerInteractor.requestHikingLayerZoomRanges() } returns flowOf(emptyList())

        viewModel = LayersViewModel(
            dispatcher = mainCoroutineRule.testDispatcher,
            hikingTileUrlProvider = tileUrlProvider,
            hikingLayerInteractor = hikingLayerInteractor
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
                            LayerType.ONLINE_HIKING_LAYER,
                            AwsHikingTileSource(tileUrlProvider, emptyList())
                        )
                    )
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
                            LayerType.ONLINE_HIKING_LAYER,
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

                viewModel.selectLayer(LayerType.ONLINE_HIKING_LAYER)

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
    fun `When init, then layer adapter items are emitted with unselected and then selected hiking layer`() =
        runTestDefault {
            viewModel.layerAdapterItems.test {
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(emptyList<LayersAdapterItem>())
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        LayersAdapterItem.Header(R.string.layers_base_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.MAPNIK,
                            titleRes = R.string.layers_mapnik_title,
                            drawableRes = R.drawable.ic_layers_mapnik,
                            isSelected = true
                        ),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.OPEN_TOPO,
                            titleRes = R.string.layers_open_topo_title,
                            drawableRes = R.drawable.ic_layers_open_topo,
                            isSelected = false
                        ),
                        LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.ONLINE_HIKING_LAYER,
                            titleRes = R.string.layers_hiking_hungarian_title,
                            drawableRes = R.drawable.ic_layers_hiking,
                            isSelected = false
                        )
                    )
                )
                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        LayersAdapterItem.Header(R.string.layers_base_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.MAPNIK,
                            titleRes = R.string.layers_mapnik_title,
                            drawableRes = R.drawable.ic_layers_mapnik,
                            isSelected = true
                        ),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.OPEN_TOPO,
                            titleRes = R.string.layers_open_topo_title,
                            drawableRes = R.drawable.ic_layers_open_topo,
                            isSelected = false
                        ),
                        LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.ONLINE_HIKING_LAYER,
                            titleRes = R.string.layers_hiking_hungarian_title,
                            drawableRes = R.drawable.ic_layers_hiking,
                            isSelected = true
                        )
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
                viewModel.selectLayer(LayerType.ONLINE_HIKING_LAYER)

                skipItems(3)

                assertThat(awaitItem()).isEqualTo(
                    listOf(
                        LayersAdapterItem.Header(R.string.layers_base_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.MAPNIK,
                            titleRes = R.string.layers_mapnik_title,
                            drawableRes = R.drawable.ic_layers_mapnik,
                            isSelected = false
                        ),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.OPEN_TOPO,
                            titleRes = R.string.layers_open_topo_title,
                            drawableRes = R.drawable.ic_layers_open_topo,
                            isSelected = true
                        ),
                        LayersAdapterItem.Header(R.string.layers_hiking_layers_header),
                        LayersAdapterItem.Layer(
                            layerType = LayerType.ONLINE_HIKING_LAYER,
                            titleRes = R.string.layers_hiking_hungarian_title,
                            drawableRes = R.drawable.ic_layers_hiking,
                            isSelected = false
                        )
                    )
                )
            }
        }

}
