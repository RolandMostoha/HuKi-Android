package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.HikingLayerFileSaveFailedException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import retrofit2.HttpException
import java.io.FileNotFoundException

object DomainExceptionMapper {

    private const val HTTP_CODE_TOO_MANY_REQUESTS = 429

    fun map(exception: Exception): DomainException {
        return when {
            exception is HttpException && exception.code() == HTTP_CODE_TOO_MANY_REQUESTS -> {
                TooManyRequestsException(exception)
            }
            exception is FileNotFoundException -> {
                HikingLayerFileSaveFailedException(exception)
            }
            else -> UnknownException(exception)
        }
    }

}
