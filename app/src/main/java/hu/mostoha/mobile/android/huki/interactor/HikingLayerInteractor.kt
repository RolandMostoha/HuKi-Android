package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.repository.HikingLayerRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class HikingLayerInteractor @Inject constructor(
    exceptionLogger: ExceptionLogger,
    private val hikingLayerRepository: HikingLayerRepository
) : BaseInteractor(exceptionLogger) {

    suspend fun requestHikingLayerFileFlow(): Flow<File?> {
        return getRequestFlow { hikingLayerRepository.getHikingLayerFile() }
    }

    suspend fun requestDownloadHikingLayerFileFlow(): Flow<Long> {
        return getRequestFlow { hikingLayerRepository.downloadHikingLayerFile() }
    }

    suspend fun requestSaveHikingLayerFileFlow(downloadId: Long): Flow<Unit> {
        return getRequestFlow { hikingLayerRepository.saveHikingLayerFile(downloadId) }
    }

}
