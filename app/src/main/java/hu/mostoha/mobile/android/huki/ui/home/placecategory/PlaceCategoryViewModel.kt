package hu.mostoha.mobile.android.huki.ui.home.placecategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.interactor.LandscapeInteractor
import hu.mostoha.mobile.android.huki.interactor.flowWithExceptions
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.center
import hu.mostoha.mobile.android.huki.model.mapper.HomeUiModelMapper
import hu.mostoha.mobile.android.huki.model.mapper.PlaceAreaMapper
import hu.mostoha.mobile.android.huki.model.ui.PlaceCategoryUiModel
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.util.WhileViewSubscribed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceCategoryViewModel @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val geocodingRepository: GeocodingRepository,
    private val landscapeInteractor: LandscapeInteractor,
    private val homeUiModelMapper: HomeUiModelMapper,
) : ViewModel() {

    private val _placeCategoryUiModel = MutableStateFlow(PlaceCategoryUiModel())
    val placeCategoryUiModel: StateFlow<PlaceCategoryUiModel> = _placeCategoryUiModel
        .stateIn(viewModelScope, WhileViewSubscribed, PlaceCategoryUiModel())

    fun init(boundingBox: BoundingBox) {
        loadLandscapes(boundingBox.center())
        loadPlaceArea(boundingBox.center(), boundingBox)
    }

    private fun loadLandscapes(location: Location) {
        viewModelScope.launch {
            landscapeInteractor.requestGetLandscapesFlow(location)
                .map { homeUiModelMapper.mapLandscapes(it) }
                .onEach { landscapes ->
                    _placeCategoryUiModel.update { it.copy(landscapes = landscapes) }
                }
                .collect()
        }
    }

    private fun loadPlaceArea(location: Location, boundingBox: BoundingBox) = viewModelScope.launch {
        flowWithExceptions(
            request = { geocodingRepository.getPlaceProfile(location) },
            exceptionLogger = exceptionLogger
        )
            .onStart {
                _placeCategoryUiModel.update {
                    it.copy(
                        isAreaLoading = true,
                        placeArea = PlaceAreaMapper.map(location, boundingBox, null)
                    )
                }
            }
            .onEach { placeProfile ->
                _placeCategoryUiModel.update {
                    it.copy(placeArea = PlaceAreaMapper.map(location, boundingBox, placeProfile))
                }
            }
            .onCompletion {
                _placeCategoryUiModel.update { it.copy(isAreaLoading = false) }
            }
            .catch {
                _placeCategoryUiModel.update {
                    it.copy(placeArea = PlaceAreaMapper.map(location, boundingBox, null))
                }
            }
            .collect()
    }

}
