package hu.mostoha.mobile.android.turistautak.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hu.mostoha.mobile.android.turistautak.executor.EspressoTestTaskExecutor
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(ApplicationComponent::class)
abstract class TestServiceModule {

    @ExperimentalCoroutinesApi
    @Binds
    abstract fun bindTaskExecutor(taskExecutor: EspressoTestTaskExecutor): TaskExecutor

}