package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import timber.log.Timber
import javax.inject.Inject

open class BaseInteractor @Inject constructor(
    private val taskExecutor: TaskExecutor,
    private val exceptionLogger: ExceptionLogger
) {

    suspend fun <T> processRequest(request: suspend () -> T): TaskResult<T> {
        return taskExecutor.runOnBackground {
            try {
                val result = request.invoke()

                TaskResult.Success(result)
            } catch (exception: Exception) {
                Timber.w(exception)

                val mappedException = if (exception is DomainException) {
                    exception
                } else {
                    DomainExceptionMapper.map(exception)
                }

                if (mappedException is UnknownException) {
                    exceptionLogger.recordException(exception)
                }

                TaskResult.Error(mappedException)
            }
        }
    }

}
