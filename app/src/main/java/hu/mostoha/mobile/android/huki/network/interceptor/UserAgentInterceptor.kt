package hu.mostoha.mobile.android.huki.network.interceptor

import android.content.Context
import android.webkit.WebSettings
import com.google.common.net.HttpHeaders
import okhttp3.Interceptor
import okhttp3.Response

/**
 * [Interceptor] which is meant to set the default user agent by using WebView's default user agent.
 * It's required for OSM tile providers and URL, deeplink resolvers.
 */
class UserAgentInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request()
                .newBuilder()
                .header(
                    name = HttpHeaders.USER_AGENT,
                    value = "${WebSettings.getDefaultUserAgent(context)}"
                )
                .build()
        )
    }

}
