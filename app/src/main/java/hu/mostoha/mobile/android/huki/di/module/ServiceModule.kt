package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.logger.FirebaseExceptionLogger
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Singleton
    @Binds
    abstract fun bindAnalyticsService(analyticsService: FirebaseAnalyticsService): AnalyticsService

    @Singleton
    @Binds
    abstract fun bindExceptionLogger(exceptionLogger: FirebaseExceptionLogger): ExceptionLogger

}
