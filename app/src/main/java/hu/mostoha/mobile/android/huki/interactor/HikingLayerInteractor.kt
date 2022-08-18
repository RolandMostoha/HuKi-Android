package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HikingLayerInteractor @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val hikingLayerRepository: HikingLayerRepository
) {

    fun requestHikingLayerZoomRanges(): Flow<List<TileZoomRange>> {
        return transformRequestToFlow(
            request = { hikingLayerRepository.getHikingLayerZoomRanges() },
            exceptionLogger = exceptionLogger
        )
    }

}
