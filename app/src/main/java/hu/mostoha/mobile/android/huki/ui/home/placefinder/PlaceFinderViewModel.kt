package hu.mostoha.mobile.android.huki.ui.home.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.interactor.toDomainException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.mapper.PlaceFinderUiModelMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MAX_HISTORY_ITEM
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaceFinderViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val appConfiguration: AppConfiguration,
    private val exceptionLogger: ExceptionLogger,
    private val myLocationProvider: AsyncMyLocationProvider,
    private val geocodingRepository: GeocodingRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val placeFinderUiModelMapper: PlaceFinderUiModelMapper,
) : ViewModel() {

    private var loadPlacesJob: Job? = null

    private val placeFinderUiModel = MutableStateFlow<PlaceFinderUiModel?>(null)

    val placeFinderItems = placeFinderUiModel.map { uiModel ->
        uiModel?.let {
            when {
                uiModel.isLoading -> {
                    emptyList<PlaceFinderItem>()
                        .plus(uiModel.staticActions)
                        .plus(uiModel.historyPlaces)
                        .plus(PlaceFinderItem.Loading)
                }
                uiModel.error != null -> {
                    emptyList<PlaceFinderItem>()
                        .plus(uiModel.staticActions)
                        .plus(uiModel.historyPlaces)
                        .plus(uiModel.error)
                }
                else -> {
                    val combinedPlaces = emptyList<PlaceFinderItem.Place>()
                        .plus(uiModel.historyPlaces)
                        .plus(uiModel.places)

                    if (combinedPlaces.isEmpty() && uiModel.searchText.length >= PLACE_FINDER_MIN_TRIGGER_LENGTH) {
                        emptyList<PlaceFinderItem>()
                            .plus(uiModel.staticActions)
                            .plus(
                                PlaceFinderItem.Info(
                                    messageRes = R.string.place_finder_empty_message.toMessage(),
                                    drawableRes = R.drawable.ic_search_bar_empty_result
                                )
                            )
                    } else {
                        emptyList<PlaceFinderItem>()
                            .plus(uiModel.staticActions)
                            .plus(combinedPlaces)
                    }
                }
            }
        }
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    fun initPlaceFinder(feature: PlaceFinderFeature) {
        loadPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        viewModelScope.launch(ioDispatcher) {
            val myLocation = myLocationProvider.getLastKnownLocationCoroutine()?.toLocation()
            val historyPlaces = placeHistoryRepository.getPlaces()
                .map { placeFinderUiModelMapper.mapHistoryItems(feature, it, myLocation) }
                .catch {
                    val error = PlaceFinderItem.Info(
                        drawableRes = R.drawable.ic_search_bar_error,
                        messageRes = it.toDomainException(exceptionLogger).messageRes
                    )
                    emit(listOf(error))
                }
                .first()

            if (placeFinderUiModel.value == null) {
                placeFinderUiModel.value = PlaceFinderUiModel(
                    searchText = "",
                    isLoading = false,
                    staticActions = listOf(PlaceFinderItem.StaticActions),
                    historyPlaces = historyPlaces,
                    places = emptyList(),
                )
            } else {
                placeFinderUiModel.update { uiModel ->
                    uiModel?.copy(
                        searchText = "",
                        isLoading = false,
                        staticActions = listOf(PlaceFinderItem.StaticActions),
                        historyPlaces = historyPlaces,
                        places = emptyList(),
                    )
                }
            }
        }
    }

    fun loadPlaces(searchText: String, placeFeature: PlaceFeature) {
        loadPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        viewModelScope.launch {
            val myLocation = myLocationProvider.getLastKnownLocationCoroutine()?.toLocation()
            val historyPlaces = placeHistoryRepository
                .getPlacesBy(searchText, PLACE_FINDER_MAX_HISTORY_ITEM)
                .map { placeFinderUiModelMapper.mapPlaceFinderItems(it, myLocation) }
                .catch {
                    val error = PlaceFinderItem.Info(
                        drawableRes = R.drawable.ic_search_bar_error,
                        messageRes = it.toDomainException(exceptionLogger).messageRes
                    )
                    emit(listOf(error))
                }
                .first()

            placeFinderUiModel.update { uiModel ->
                uiModel?.copy(
                    searchText = searchText,
                    staticActions = emptyList(),
                    historyPlaces = historyPlaces,
                )
            }

            loadPlacesJob = viewModelScope.launch {
                flowWithExceptions(
                    request = { geocodingRepository.getPlacesBy(searchText, placeFeature, myLocation) },
                    exceptionLogger = exceptionLogger
                )
                    .map { placeFinderUiModelMapper.mapPlaceFinderItems(it, myLocation) }
                    .onEach { places ->
                        placeFinderUiModel.update { uiModel ->
                            uiModel?.copy(
                                isLoading = false,
                                places = places
                            )
                        }
                    }
                    .onStart {
                        placeFinderUiModel.update { it?.copy(isLoading = true) }

                        delay(appConfiguration.getSearchQueryDelay())
                    }
                    .catch { throwable ->
                        if (throwable is DomainException && throwable !is JobCancellationException) {
                            placeFinderUiModel.update { uiModel ->
                                uiModel?.copy(
                                    isLoading = false,
                                    error = PlaceFinderItem.Info(
                                        drawableRes = R.drawable.ic_search_bar_error,
                                        messageRes = throwable.messageRes,
                                    ),
                                    places = emptyList()
                                )
                            }
                        }
                        Timber.e(throwable)
                    }
                    .collect()
            }
        }
    }

    fun cancelSearch() {
        loadPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        placeFinderUiModel.value = null
    }

}
