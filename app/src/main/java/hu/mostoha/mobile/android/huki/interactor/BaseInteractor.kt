package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

open class BaseInteractor @Inject constructor(
    private val exceptionLogger: ExceptionLogger
) {

    fun <T> getRequestFlow(request: suspend () -> T): Flow<T> {
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

                if (!BuildConfig.DEBUG && mappedException is UnknownException) {
                    exceptionLogger.recordException(exception)
                }

                throw mappedException
            }
        }
    }

}
