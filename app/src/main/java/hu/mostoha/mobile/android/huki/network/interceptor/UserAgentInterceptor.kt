package hu.mostoha.mobile.android.huki.network.interceptor

import android.content.Context
import android.webkit.WebSettings
import com.google.common.net.HttpHeaders
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * [Interceptor] which sets the User-Agent header.
 *
 * For Overpass API requests it sends a custom, app-identifying User-Agent, which is required by the
 * overpass-api.de usage policy (stock UAs are rejected with HTTP 406).
 * For all other hosts (OSM tile providers, URL/deeplink resolvers) it keeps WebView's default user agent.
 */
class UserAgentInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val userAgent = if (request.url.host == OVERPASS_HOST) {
            OVERPASS_USER_AGENT
        } else {
            WebSettings.getDefaultUserAgent(context)
        }

        return chain.proceed(
            request.newBuilder()
                .header(
                    name = HttpHeaders.USER_AGENT,
                    value = userAgent
                )
                .build()
        )
    }

    companion object {
        private val OVERPASS_HOST = NetworkConfig.BASE_URL_OVERPASS.toHttpUrlOrNull()?.host

        private val OVERPASS_USER_AGENT =
            "HuKi/${BuildConfig.VERSION_NAME} (Android; ${BuildConfig.APPLICATION_ID})"
    }

}
