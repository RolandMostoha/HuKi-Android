package hu.mostoha.mobile.android.turistautak.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hu.mostoha.mobile.android.turistautak.executor.DefaultTaskExecutor
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor

@Module
@InstallIn(ApplicationComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindTaskExecutor(taskExecutor: DefaultTaskExecutor): TaskExecutor

}