package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * Transforms the suspend requests to [Flow]s.
 * If there is an execution error it logs and records exceptions via Crashlytics.
 *
 * @param T the return value of the request.
 * @param request the executable suspend request.
 * @param exceptionLogger the logger for recording execution errors.
 * @return the request wrapped in a [Flow].
 */
fun <T> flowWithExceptions(request: suspend () -> T, exceptionLogger: ExceptionLogger): Flow<T> {
    return flow {
        try {
            val result = request.invoke()

            emit(result)
        } catch (exception: Exception) {
            Timber.w(exception)

            val mappedException = if (exception is DomainException) {
                exception
            } else {
                DomainExceptionMapper.map(exception)
            }

            if (!BuildConfig.DEBUG && mappedException !is JobCancellationException) {
                exceptionLogger.recordException(exception)
            }

            throw mappedException
        }
    }
}
