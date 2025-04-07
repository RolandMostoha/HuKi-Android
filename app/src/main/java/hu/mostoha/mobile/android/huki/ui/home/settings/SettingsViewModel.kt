package hu.mostoha.mobile.android.huki.ui.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import hu.mostoha.mobile.android.huki.util.toScaleFromPercentage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val versionConfiguration: VersionConfiguration,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    val mapScaleFactor: StateFlow<Double> = settingsRepository.getMapScaleFactor()
        .stateIn(viewModelScope, WhileViewSubscribed, MAP_DEFAULT_SCALE_FACTOR)

    val theme: StateFlow<Theme> = settingsRepository.getTheme()
        .stateIn(viewModelScope, WhileViewSubscribed, Theme.SYSTEM)

    val newFeatures: StateFlow<String?> = versionConfiguration.getNewFeatures(BuildConfig.VERSION_NAME)
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    fun updateMapScale(percentage: Int) {
        viewModelScope.launch {
            settingsRepository.saveMapScaleFactor(percentage.toScaleFromPercentage())
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            val actualTheme = this@SettingsViewModel.theme.value

            if (actualTheme != theme) {
                settingsRepository.saveTheme(theme)
                analyticsService.settingsThemeClicked(theme)
            }
        }
    }

    fun updateIsGpxSlopeColoringEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveGpxSlopeColoringEnabled(isEnabled)
        }
    }

    fun updateNewFeaturesSeen(version: String) {
        viewModelScope.launch {
            versionConfiguration.saveNewFeaturesSeen(version)
            analyticsService.newFeaturesSeen(version)
        }
    }

}
