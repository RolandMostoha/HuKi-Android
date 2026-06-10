package hu.mostoha.mobile.android.huki.interactor

import hu.mostoha.mobile.android.huki.interactor.exception.DomainException
import hu.mostoha.mobile.android.huki.interactor.exception.GpxParseFailedException
import hu.mostoha.mobile.android.huki.interactor.exception.JobCancellationException
import hu.mostoha.mobile.android.huki.interactor.exception.TimeoutException
import hu.mostoha.mobile.android.huki.interactor.exception.TooManyRequestsException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownException
import hu.mostoha.mobile.android.huki.interactor.exception.UnknownHostDomainException
import kotlinx.coroutines.CancellationException
import org.xmlpull.v1.XmlPullParserException
import java.io.FileNotFoundException
import java.net.UnknownHostException

object DomainExceptionMapper {

    fun map(exception: Exception): DomainException {
        return when {
            exception.isTooManyRequests() -> {
                TooManyRequestsException(exception)
            }
            exception.isTimeoutError() -> {
                TimeoutException(exception)
            }
            exception is UnknownHostException -> {
                UnknownHostDomainException(exception)
            }
            exception is CancellationException -> {
                JobCancellationException(exception)
            }
            exception is FileNotFoundException || exception is XmlPullParserException -> {
                GpxParseFailedException(exception)
            }
            else -> UnknownException(exception)
        }
    }
}
