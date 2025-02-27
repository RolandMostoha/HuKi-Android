package hu.mostoha.mobile.android.huki.di.module

import android.content.Context
import android.webkit.WebSettings
import com.google.common.net.HttpHeaders
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.network.GraphhopperService
import hu.mostoha.mobile.android.huki.network.LocationIqService
import hu.mostoha.mobile.android.huki.network.NetworkConfig
import hu.mostoha.mobile.android.huki.network.OverpassService
import hu.mostoha.mobile.android.huki.network.interceptor.TimeoutInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                chain.proceed(
                    chain.request()
                        .newBuilder()
                        .header(
                            name = HttpHeaders.USER_AGENT,
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
            .addInterceptor(TimeoutInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Singleton
    @Provides
    fun provideOverpassService(okHttpClient: OkHttpClient, moshi: Moshi): OverpassService {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL_OVERPASS)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OverpassService::class.java)
    }

    @Singleton
    @Provides
    fun provideLocationIqService(okHttpClient: OkHttpClient, moshi: Moshi): LocationIqService {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL_LOCATION_IQ)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocationIqService::class.java)
    }

    @Singleton
    @Provides
    fun provideGraphhopperService(okHttpClient: OkHttpClient, moshi: Moshi): GraphhopperService {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL_GRAPHHOPPER_API)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GraphhopperService::class.java)
    }

}
