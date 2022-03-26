package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LandscapeInteractor @Inject constructor(
    exceptionLogger: ExceptionLogger,
    private val landscapeRepository: LandscapeRepository
) : BaseInteractor(exceptionLogger) {

    suspend fun requestGetLandscapesFlow(): Flow<List<Landscape>> {
        return getRequestFlow { landscapeRepository.getLandscapes() }
    }

}
