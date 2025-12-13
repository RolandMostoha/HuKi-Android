package hu.mostoha.mobile.android.huki.service

import android.content.Context
import android.util.Patterns
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.mostoha.mobile.android.huki.di.module.IoDispatcher
import hu.mostoha.mobile.android.huki.network.interceptor.UserAgentInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

class ShortUrlResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    companion object Companion {
        private val redirectHttpCodes = listOf(
            301, 302, 303, 307, 308
        )
    }

    private val okHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .addNetworkInterceptor(UserAgentInterceptor(context))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    suspend fun resolveShortUrl(shortUrl: String): String? {
        return withContext(ioDispatcher) {
            val request = Request.Builder()
                .url(shortUrl)
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (redirectHttpCodes.contains(response.code)) {
                val longUrl = response.header("Location")

                Timber.d("UrlResolver: resolved \nshortUrl:$shortUrl \nlongUrl:$longUrl")

                longUrl
            } else {
                Timber.d("UrlResolver: cannot resolve shortUrl: $shortUrl, non-redirect HTTP code")

                null
            }
        }
    }

    fun extractUrls(url: String): List<String> {
        val matcher = Patterns.WEB_URL.matcher(url)
        val urls = mutableListOf<String>()

        while (matcher.find()) {
            urls.add(matcher.group())
        }

        return urls
    }

}
