package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.executor.TaskExecutor
import timber.log.Timber
import javax.inject.Inject

open class BaseInteractor @Inject constructor(private val taskExecutor: TaskExecutor) {

    suspend fun <T> processRequest(
        request: suspend () -> T,
        domainExceptionMapper: ((Exception) -> DomainException)? = null
    ): TaskResult<T> {
        return taskExecutor.runOnBackground {
            try {
                val result = request.invoke()

                TaskResult.Success(result)
            } catch (exception: Exception) {
                Timber.w(exception)

                val domainException = domainExceptionMapper?.invoke(exception)

                val generalDomainException = GeneralDomainExceptionMapper.map(exception)

                TaskResult.Error(domainException ?: generalDomainException)
            }
        }
    }

}
