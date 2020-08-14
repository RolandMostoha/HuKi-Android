package hu.mostoha.mobile.android.turistautak.di.module

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hu.mostoha.mobile.android.turistautak.network.NetworkConfig
import hu.mostoha.mobile.android.turistautak.network.OverpassService
import hu.mostoha.mobile.android.turistautak.network.PhotonService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


@Module
@InstallIn(ApplicationComponent::class)
class NetworkModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .readTimeout(NetworkConfig.TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            .connectTimeout(NetworkConfig.TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
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