package hu.mostoha.mobile.android.huki.interactor

import android.net.Uri
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
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
        return transformRequestToFlow(
            request = { layersRepository.getHikingLayerZoomRanges() },
            exceptionLogger = exceptionLogger
        )
    }

    fun requestGpxDetails(fileUri: Uri?): Flow<GpxDetails> {
        return transformRequestToFlow(
            request = { layersRepository.getGpxDetails(fileUri) },
            exceptionLogger = exceptionLogger
        )
    }

}