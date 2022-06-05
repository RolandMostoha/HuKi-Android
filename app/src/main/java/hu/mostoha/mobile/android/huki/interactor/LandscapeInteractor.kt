package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.util.distanceBetween
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LandscapeInteractor @Inject constructor(
    exceptionLogger: ExceptionLogger,
    private val landscapeRepository: LandscapeRepository
) : BaseInteractor(exceptionLogger) {

    suspend fun requestGetLandscapesFlow(location: Location? = null): Flow<List<Landscape>> {
        val landscapesFlow = getRequestFlow { landscapeRepository.getLandscapes() }

        return if (location == null) {
            landscapesFlow
        } else {
            landscapesFlow.map { landscapes ->
                landscapes.sortedBy { location.distanceBetween(it.center) }
            }
        }
    }

}
