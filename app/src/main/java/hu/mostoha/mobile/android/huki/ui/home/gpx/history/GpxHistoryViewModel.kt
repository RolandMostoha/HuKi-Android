package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.interactor.transformRequestToFlow
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.GpxHistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GpxHistoryViewModel @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val layersRepository: LayersRepository,
    private val gpxHistoryUiModelMapper: GpxHistoryUiModelMapper,
) : ViewModel() {

    private val _currentTab = MutableStateFlow(GpxHistoryTab.ROUTE_PLANNER)
    val currentTab: StateFlow<GpxHistoryTab> = _currentTab
        .stateIn(viewModelScope, WhileViewSubscribed, GpxHistoryTab.ROUTE_PLANNER)

    private val gpxHistory = transformRequestToFlow(
        request = { layersRepository.getGpxHistory() },
        exceptionLogger = exceptionLogger
    )
        .map { gpxHistoryUiModelMapper.mapToUiModel(it) }
        .catch { throwable ->
            Timber.e(throwable, "Error while loading gpx history")

            exceptionLogger.recordException(throwable)

            val errorItem = GpxHistoryAdapterModel.InfoView(
                message = R.string.gpx_history_item_error,
                iconRes = R.drawable.ic_gpx_history_error,
            )
            emit(GpxHistoryUiModel(listOf(errorItem), listOf(errorItem)))
        }
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    val gpxHistoryAdapterItems = currentTab.combine(gpxHistory.filterNotNull()) { currentTab, gpxHistory ->
        when (currentTab) {
            GpxHistoryTab.ROUTE_PLANNER -> gpxHistory.routePlannerGpxList
            GpxHistoryTab.EXTERNAL -> gpxHistory.externalGpxList
        }
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    fun tabSelected(gpxHistoryTab: GpxHistoryTab) {
        _currentTab.value = gpxHistoryTab
    }

}
