package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.configuration.HukiAppConfiguration
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigurationModule {

    @Singleton
    @Binds
    abstract fun bindAppConfiguration(appConfiguration: HukiAppConfiguration): AppConfiguration

    @Singleton
    @Binds
    abstract fun bindGpxConfiguration(appConfiguration: HukiGpxConfiguration): GpxConfiguration

}
