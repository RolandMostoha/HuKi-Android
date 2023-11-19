package hu.mostoha.mobile.android.huki.ui.home.history.gpx

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.extensions.removeFileExtension
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.HistoryUiModelMapper
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
    private val historyUiModelMapper: HistoryUiModelMapper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage.asSharedFlow()

    private val _gpxHistory = MutableStateFlow<GpxHistoryUiModel?>(null)
    val gpxHistory: StateFlow<GpxHistoryUiModel?> = _gpxHistory
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    val gpxHistoryFileNames = _gpxHistory.map { gpxHistory ->
        gpxHistory?.let {
            gpxHistory.routePlannerGpxList
                .plus(gpxHistory.externalGpxList)
                .filterIsInstance<GpxHistoryAdapterModel.Item>()
                .mapNotNull { it.fileUri.lastPathSegment?.removeFileExtension() }
        } ?: emptyList()
    }

    init {
        refreshGpxHistory()
    }

    fun deleteGpx(fileUri: Uri) {
        viewModelScope.launch(dispatcher) {
            flowWithExceptions(
                request = { layersRepository.deleteGpx(fileUri) },
                exceptionLogger = exceptionLogger
            )
                .catch { throwable ->
                    Timber.e(throwable, "Error while deleting GPX")

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
                    Timber.e(throwable, "Error while renaming GPX")

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
                .map { historyUiModelMapper.mapGpxHistory(it) }
                .onEach { _gpxHistory.emit(it) }
                .catch { throwable ->
                    Timber.e(throwable, "Error while loading gpx history")

                    val errorItem = GpxHistoryAdapterModel.InfoView(
                        message = R.string.gpx_history_item_error,
                        iconRes = R.drawable.ic_gpx_history_error,
                    )

                    _gpxHistory.emit(GpxHistoryUiModel(listOf(errorItem), listOf(errorItem)))
                }
                .collect()
        }
    }

}
