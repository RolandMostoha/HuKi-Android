package hu.mostoha.mobile.android.huki.di.module

import android.content.Context
import android.webkit.WebSettings
import com.amazonaws.http.HttpHeader
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.network.PhotonService
import hu.mostoha.mobile.android.huki.network.adapter.SymbolTypeAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header(
                            name = HttpHeader.USER_AGENT,
                            value = "${WebSettings.getDefaultUserAgent(context)} ${BuildConfig.APPLICATION_ID}"
                        )
                        .build()
                )
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .readTimeout(NetworkConfig.TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .connectTimeout(NetworkConfig.TIMEOUT_SEC.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(SymbolTypeAdapter)
            .build()
    }

    @Provides
    fun provideOverpassService(okHttpClient: OkHttpClient, moshi: Moshi): OverpassService {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL_OVERPASS)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OverpassService::class.java)
    }

    @Provides
    fun providePhotonService(okHttpClient: OkHttpClient, moshi: Moshi): PhotonService {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL_PHOTON)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PhotonService::class.java)
    }

}
