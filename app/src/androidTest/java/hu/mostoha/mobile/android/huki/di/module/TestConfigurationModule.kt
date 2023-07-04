package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import hu.mostoha.mobile.android.huki.configuration.AppConfiguration
import hu.mostoha.mobile.android.huki.configuration.GpxConfiguration
import hu.mostoha.mobile.android.huki.configuration.HukiGpxConfiguration
import hu.mostoha.mobile.android.huki.configuration.TestAppConfiguration
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ConfigurationModule::class]
)
abstract class TestConfigurationModule {

    @Singleton
    @Binds
    abstract fun bindAppConfiguration(appConfiguration: TestAppConfiguration): AppConfiguration

    @Singleton
    @Binds
    abstract fun bindGpxConfiguration(gpxConfiguration: HukiGpxConfiguration): GpxConfiguration

}
