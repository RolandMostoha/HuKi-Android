package hu.mostoha.mobile.android.huki.interactor

import retrofit2.HttpException
import java.net.SocketTimeoutException

const val HTTP_CODE_TOO_MANY_REQUESTS = 429
const val HTTP_CODE_BAD_GATEWAY = 502
const val HTTP_CODE_GATEWAY_TIMEOUT = 504

private fun Throwable.httpCode(): Int? = (this as? HttpException)?.code()

fun Throwable.isTooManyRequests(): Boolean = httpCode() == HTTP_CODE_TOO_MANY_REQUESTS

/**
 * A gateway timeout (504) or a client-side socket timeout.
 */
fun Throwable.isTimeoutError(): Boolean {
    return this is SocketTimeoutException || httpCode() == HTTP_CODE_GATEWAY_TIMEOUT
}

/**
 * Transient Overpass errors that are safe to retry: timeouts and bad gateway responses. The first
 * (cold) request warms the server's page cache for the queried area, so an immediate retry usually
 * succeeds. 429 (Too Many Requests) is intentionally excluded — it must be respected, not retried.
 */
fun Throwable.isRetriableOverpassError(): Boolean {
    return isTimeoutError() || httpCode() == HTTP_CODE_BAD_GATEWAY
}
