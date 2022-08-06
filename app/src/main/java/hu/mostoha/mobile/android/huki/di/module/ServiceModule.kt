package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindAnalyticsService(analyticsService: FirebaseAnalyticsService): AnalyticsService

}
