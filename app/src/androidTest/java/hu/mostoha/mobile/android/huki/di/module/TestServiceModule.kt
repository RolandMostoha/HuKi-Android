package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.service.FakeAnalyticsService

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class TestServiceModule {

    @Binds
    abstract fun bindAnalyticsService(analyticsService: FakeAnalyticsService): AnalyticsService

}
