package hu.mostoha.mobile.android.huki.interactor

import android.net.Uri
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.GpxDetails
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LayersInteractor @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val layersRepository: LayersRepository
) {

    fun requestHikingLayerZoomRanges(): Flow<List<TileZoomRange>> {
        return flowWithExceptions(
            request = { layersRepository.getHikingLayerZoomRanges() },
            exceptionLogger = exceptionLogger
        )
    }

    fun requestGpxDetails(fileUri: Uri?): Flow<GpxDetails> {
        return flowWithExceptions(
            request = { layersRepository.getGpxDetails(fileUri) },
            exceptionLogger = exceptionLogger
        )
    }

    fun requestRoutePlannerGpxDetails(fileUri: Uri): Flow<GpxDetails> {
        return flowWithExceptions(
            request = { layersRepository.getRoutePlannerGpxDetails(fileUri) },
            exceptionLogger = exceptionLogger
        )
    }

}
