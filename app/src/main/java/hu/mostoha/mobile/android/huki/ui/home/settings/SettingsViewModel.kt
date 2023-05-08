package hu.mostoha.mobile.android.huki.ui.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import hu.mostoha.mobile.android.huki.util.toScaleFromPercentage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val mapScaleFactor: StateFlow<Double> = settingsRepository.getMapScaleFactor()
        .stateIn(viewModelScope, WhileViewSubscribed, MAP_DEFAULT_SCALE_FACTOR)

    fun updateMapScale(percentage: Int) {
        viewModelScope.launch {
            settingsRepository.saveMapScaleFactor(percentage.toScaleFromPercentage())
        }
    }

}
