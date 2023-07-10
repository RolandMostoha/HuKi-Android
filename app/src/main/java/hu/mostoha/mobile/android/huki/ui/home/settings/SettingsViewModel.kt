package hu.mostoha.mobile.android.huki.ui.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
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
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    val mapScaleFactor: StateFlow<Double> = settingsRepository.getMapScaleFactor()
        .stateIn(viewModelScope, WhileViewSubscribed, MAP_DEFAULT_SCALE_FACTOR)

    val theme: StateFlow<Theme> = settingsRepository.getTheme()
        .stateIn(viewModelScope, WhileViewSubscribed, Theme.SYSTEM)

    fun updateMapScale(percentage: Int) {
        viewModelScope.launch {
            settingsRepository.saveMapScaleFactor(percentage.toScaleFromPercentage())
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            val actualTheme = this@SettingsViewModel.theme.value

            if (actualTheme != theme) {
                analyticsService.settingsThemeClicked(theme)
                settingsRepository.saveTheme(theme)
            }
        }
    }

}
