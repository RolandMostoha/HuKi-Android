package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.GatewayTimeoutException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import retrofit2.HttpException

object DomainExceptionMapper {

    private const val HTTP_CODE_TOO_MANY_REQUESTS = 429
    private const val HTTP_CODE_GATEWAY_TIMEOUT = 504

    fun map(exception: Exception): DomainException {
        return when {
            exception is HttpException && exception.code() == HTTP_CODE_TOO_MANY_REQUESTS -> {
                TooManyRequestsException(exception)
            }
            exception is HttpException && exception.code() == HTTP_CODE_GATEWAY_TIMEOUT -> {
                GatewayTimeoutException(exception)
            }
            else -> UnknownException(exception)
        }
    }

}
