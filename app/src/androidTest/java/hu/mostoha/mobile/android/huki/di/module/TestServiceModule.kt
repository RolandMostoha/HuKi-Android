package hu.mostoha.mobile.android.huki.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.service.FakeAnalyticsService
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ServiceModule::class]
)
class TestServiceModule {

    @Singleton
    @Provides
    fun provideAnalyticsService(): AnalyticsService = FakeAnalyticsService()

}
