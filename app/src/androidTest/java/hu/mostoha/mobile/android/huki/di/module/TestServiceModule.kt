package hu.mostoha.mobile.android.huki.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import hu.mostoha.mobile.android.huki.fake.FakeAnalyticsService
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.logger.FakeExceptionLogger
import hu.mostoha.mobile.android.huki.service.AnalyticsService
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

    @Singleton
    @Provides
    fun provideExceptionLogger(): ExceptionLogger = FakeExceptionLogger()

}
