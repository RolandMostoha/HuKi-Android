package hu.mostoha.mobile.android.huki.ui.home.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.di.module.DefaultDispatcher
import hu.mostoha.mobile.android.huki.interactor.PlacesInteractor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.mapper.PlaceFinderUiModelMapper
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceFinderViewModel @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val appConfiguration: AppConfiguration,
    private val placesInteractor: PlacesInteractor,
    private val myLocationProvider: AsyncMyLocationProvider,
    private val placeFinderUiModelMapper: PlaceFinderUiModelMapper
) : ViewModel() {

    private var loadPlacesJob: Job? = null

    private val _placeFinderItems = MutableStateFlow<List<PlaceFinderItem>?>(null)
    val placeFinderItems: StateFlow<List<PlaceFinderItem>?> = _placeFinderItems
        .stateIn(viewModelScope, WhileViewSubscribed, null)

    fun initStaticActions() {
        viewModelScope.launch(defaultDispatcher) {
            val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()?.toLocation()
            val staticActions = placeFinderUiModelMapper.getStaticActions(lastKnownLocation != null)

            _placeFinderItems.value = staticActions
        }
    }

    fun loadPlaces(searchText: String) {
        loadPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        loadPlacesJob = viewModelScope.launch {
            val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()?.toLocation()

            placesInteractor.requestGetPlacesByFlow(searchText, lastKnownLocation)
                .map { placeFinderUiModelMapper.mapPlaceFinderItems(it, lastKnownLocation) }
                .onEach { _placeFinderItems.emit(it) }
                .catch { throwable ->
                    if (throwable is DomainException && throwable !is JobCancellationException) {
                        _placeFinderItems.emit(placeFinderUiModelMapper.mapPlacesErrorItem(throwable))
                    }
                }
                .onStart {
                    val staticActions = placeFinderUiModelMapper.getStaticActions(lastKnownLocation != null)

                    _placeFinderItems.emit(staticActions.plus(PlaceFinderItem.Loading))

                    delay(appConfiguration.getSearchQueryDelay())
                }
                .collect()
        }
    }

    fun cancelSearch() {
        loadPlacesJob?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
        }
        _placeFinderItems.value = null
    }

}
