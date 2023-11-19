package hu.mostoha.mobile.android.huki.ui.home.history.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.interactor.toDomainException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.mapper.HistoryUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaceHistoryViewModel @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val historyUiModelMapper: HistoryUiModelMapper,
    private val dateTimeProvider: DateTimeProvider,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<Message.Res>()
    val errorMessage: SharedFlow<Message.Res> = _errorMessage.asSharedFlow()

    val placeHistory = placeHistoryRepository.getPlaces()
        .map { historyUiModelMapper.mapPlaceHistory(it, dateTimeProvider.now()) }
        .catch { showError(it) }
        .stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    fun deletePlace(osmId: String) {
        viewModelScope.launch {
            runCatching { placeHistoryRepository.deletePlace(osmId) }
                .onFailure { showError(it) }
        }
    }

    private suspend fun showError(throwable: Throwable) {
        Timber.e(throwable)

        val mappedException = throwable.toDomainException(exceptionLogger)

        _errorMessage.emit(mappedException.messageRes)
    }

}
