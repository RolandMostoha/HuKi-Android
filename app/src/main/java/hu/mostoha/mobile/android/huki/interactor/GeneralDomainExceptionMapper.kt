package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.ui.util.toMessage
import retrofit2.HttpException

object GeneralDomainExceptionMapper {

    private const val HTTP_CODE_TOO_MANY_REQUESTS = 429

    fun map(exception: Exception): DomainException {
        return when {
            exception is HttpException && exception.code() == HTTP_CODE_TOO_MANY_REQUESTS -> {
                DomainException(
                    throwable = exception,
                    messageRes = R.string.error_message_too_many_requests.toMessage()
                )
            }
            else -> {
                DomainException(
                    throwable = exception,
                    messageRes = R.string.error_message_unknown.toMessage()
                )
            }
        }
    }

}
