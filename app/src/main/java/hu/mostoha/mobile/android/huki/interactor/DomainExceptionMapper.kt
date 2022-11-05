package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.GatewayTimeoutException
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownHostDomainException
import kotlinx.coroutines.CancellationException
import org.xmlpull.v1.XmlPullParserException
import retrofit2.HttpException
import java.io.FileNotFoundException
import java.net.UnknownHostException

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
            exception is UnknownHostException -> {
                UnknownHostDomainException(exception)
            }
            exception is CancellationException -> {
                JobCancellationException(exception)
            }
            exception is FileNotFoundException -> {
                GpxParseFailedException(exception)
            }
            exception is XmlPullParserException -> {
                GpxParseFailedException(exception)
            }
            else -> UnknownException(exception)
        }
    }

}
