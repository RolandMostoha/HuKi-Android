package hu.mostoha.mobile.android.huki.network.interceptor

import hu.mostoha.mobile.android.huki.network.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * [Interceptor] which is capable of setting the timeout for each network API request via Header parameter
 */
class TimeoutInterceptor : Interceptor {

    companion object {
        const val HEADER_TIMEOUT = "Timeout"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headerTimeout = request.header(HEADER_TIMEOUT)
        val timeout = headerTimeout?.toIntOrNull() ?: NetworkConfig.DEFAULT_TIMEOUT_MS

        return chain
            .withConnectTimeout(timeout, TimeUnit.MILLISECONDS)
            .withReadTimeout(timeout, TimeUnit.MILLISECONDS)
            .withWriteTimeout(timeout, TimeUnit.MILLISECONDS)
            .proceed(request)
    }

}
