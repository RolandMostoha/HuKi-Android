package hu.mostoha.mobile.android.huki.ui.home.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    private val settingsRepository = mockk<SettingsRepository>()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { settingsRepository.getMapScaleFactor() } returns flowOf(2.0f)

        viewModel = SettingsViewModel(settingsRepository)
    }

    @Test
    fun `When initWaypoints, then empty waypoints are emitted`() {
        runTestDefault {
            viewModel.mapScaleFactor.test {
                assertThat(awaitItem()).isEqualTo(MAP_DEFAULT_SCALE_FACTOR)
                assertThat(awaitItem()).isEqualTo(2.0f)
            }
        }
    }

}
