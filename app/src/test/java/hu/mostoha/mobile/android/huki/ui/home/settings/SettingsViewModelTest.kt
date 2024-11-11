package hu.mostoha.mobile.android.huki.ui.home.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.MainCoroutineRule
import hu.mostoha.mobile.android.huki.util.runTestDefault
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel

    private val settingsRepository = mockk<SettingsRepository>()
    private val versionConfiguration = mockk<VersionConfiguration>()
    private val analyticsService = mockk<AnalyticsService>(relaxed = true)

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        every { settingsRepository.getMapScaleFactor() } returns flowOf(2.0)
        every { settingsRepository.getTheme() } returns flowOf(Theme.SYSTEM)
        coEvery { versionConfiguration.getNewFeatures(any()) } returns flowOf(null)

        viewModel = SettingsViewModel(
            settingsRepository,
            versionConfiguration,
            analyticsService
        )
    }

    @Test
    fun `When initWaypoints, then empty waypoints are emitted`() {
        runTestDefault {
            viewModel.mapScaleFactor.test {
                assertThat(awaitItem()).isEqualTo(MAP_DEFAULT_SCALE_FACTOR)
                assertThat(awaitItem()).isEqualTo(2.0)
            }
        }
    }

    @Test
    fun `Given percentage, when updateMapScale, then empty waypoints are emitted`() {
        runTestDefault {
            val mapScaleFactor = 2.1

            coEvery { settingsRepository.saveMapScaleFactor(mapScaleFactor) } returns Unit

            viewModel.updateMapScale(210)

            advanceUntilIdle()

            coVerify { settingsRepository.saveMapScaleFactor(mapScaleFactor) }
        }
    }

    @Test
    fun `Given theme different than actual theme, when updateTheme, then theme is saved`() {
        runTestDefault {
            coEvery { settingsRepository.saveTheme(any()) } returns Unit

            viewModel.updateTheme(Theme.LIGHT)

            advanceUntilIdle()

            coVerify { settingsRepository.saveTheme(Theme.LIGHT) }
        }
    }

    @Test
    fun `Given the same theme than the actual one, when updateTheme, then theme is not saved`() {
        runTestDefault {
            coEvery { settingsRepository.saveTheme(any()) } returns Unit

            viewModel.updateTheme(Theme.SYSTEM)

            advanceUntilIdle()

            coVerify(inverse = true) { settingsRepository.saveTheme(Theme.LIGHT) }
        }
    }

    @Test
    fun `Given version, when updateNewFeaturesSeen, then version is saved`() {
        runTestDefault {
            val version = "v1.0.5"
            coEvery { versionConfiguration.saveNewFeaturesSeen(any()) } returns Unit

            viewModel.updateNewFeaturesSeen(version)

            advanceUntilIdle()

            coVerify { versionConfiguration.saveNewFeaturesSeen(version) }
        }
    }

}
