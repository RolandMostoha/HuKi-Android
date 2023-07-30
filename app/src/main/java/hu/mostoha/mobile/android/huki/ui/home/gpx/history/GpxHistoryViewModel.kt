package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.removeFileExtension
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.GpxHistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.GpxHistoryUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GpxHistoryViewModel @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val layersRepository: LayersRepository,
    private val gpxHistoryUiModelMapper: GpxHistoryUiModelMapper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage.asSharedFlow()

    private val _currentTab = MutableStateFlow(GpxHistoryTab.ROUTE_PLANNER)
    val currentTab: StateFlow<GpxHistoryTab> = _currentTab
        .stateIn(viewModelScope, WhileViewSubscribed, GpxHistoryTab.ROUTE_PLANNER)

    private val gpxHistory = MutableStateFlow<GpxHistoryUiModel?>(null)

    val gpxHistoryAdapterItems = currentTab.combine(gpxHistory.filterNotNull()) { currentTab, gpxHistory ->
        when (currentTab) {
            GpxHistoryTab.ROUTE_PLANNER -> gpxHistory.routePlannerGpxList
            GpxHistoryTab.EXTERNAL -> gpxHistory.externalGpxList
        }
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    val gpxHistoryFileNames = gpxHistory.map { gpxHistory ->
        if (gpxHistory == null) {
            return@map emptyList<String>()
        }

        gpxHistory.routePlannerGpxList
            .plus(gpxHistory.externalGpxList)
            .filterIsInstance<GpxHistoryAdapterModel.Item>()
            .mapNotNull { it.fileUri.lastPathSegment?.removeFileExtension() }
    }

    init {
        refreshGpxHistory()
    }

    fun tabSelected(gpxHistoryTab: GpxHistoryTab) {
        _currentTab.value = gpxHistoryTab
    }

    fun deleteGpx(fileUri: Uri) {
        viewModelScope.launch(dispatcher) {
            flowWithExceptions(
                request = { layersRepository.deleteGpx(fileUri) },
                exceptionLogger = exceptionLogger
            )
                .catch { throwable ->
                    Timber.e(throwable, "Error while renaming gpx")

                    _errorMessage.emit(Message.Res(R.string.gpx_history_rename_operation_error))
                }
                .onCompletion { refreshGpxHistory() }
                .collect()
        }
    }

    fun renameGpx(result: GpxRenameResult) {
        viewModelScope.launch(dispatcher) {
            flowWithExceptions(
                request = { layersRepository.renameGpx(result.gpxUri, result.newName) },
                exceptionLogger = exceptionLogger
            )
                .catch { throwable ->
                    Timber.e(throwable, "Error while renaming gpx")

                    _errorMessage.emit(Message.Res(R.string.gpx_history_rename_operation_error))
                }
                .onCompletion { refreshGpxHistory() }
                .collect()
        }
    }

    private fun refreshGpxHistory() {
        viewModelScope.launch {
            flowWithExceptions(
                request = { layersRepository.getGpxHistory() },
                exceptionLogger = exceptionLogger
            )
                .map { gpxHistoryUiModelMapper.mapToUiModel(it) }
                .onEach { gpxHistory.emit(it) }
                .catch { throwable ->
                    Timber.e(throwable, "Error while loading gpx history")

                    val errorItem = GpxHistoryAdapterModel.InfoView(
                        message = R.string.gpx_history_item_error,
                        iconRes = R.drawable.ic_gpx_history_error,
                    )

                    gpxHistory.emit(GpxHistoryUiModel(listOf(errorItem), listOf(errorItem)))
                }
                .collect()
        }
    }

}
