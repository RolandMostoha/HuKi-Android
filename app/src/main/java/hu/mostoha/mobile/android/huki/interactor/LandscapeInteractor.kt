package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.model.domain.Landscape
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.util.distanceBetween
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LandscapeInteractor @Inject constructor(
    private val exceptionLogger: ExceptionLogger,
    private val landscapeRepository: LandscapeRepository
) {

    fun requestGetLandscapesFlow(location: Location? = null): Flow<List<Landscape>> {
        val landscapesFlow = flowWithExceptions(
            request = { landscapeRepository.getLandscapes() },
            exceptionLogger = exceptionLogger
        )

        return if (location == null) {
            landscapesFlow
        } else {
            landscapesFlow.map { landscapes ->
                landscapes.sortedBy { location.distanceBetween(it.center) }
            }
        }
    }

}
