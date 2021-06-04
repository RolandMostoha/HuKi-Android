package hu.mostoha.mobile.android.turistautak.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.turistautak.executor.EspressoTestTaskExecutor
import hu.mostoha.mobile.android.turistautak.executor.TaskExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
@InstallIn(SingletonComponent::class)
abstract class TestServiceModule {

    @ExperimentalCoroutinesApi
    @Binds
    abstract fun bindTaskExecutor(taskExecutor: EspressoTestTaskExecutor): TaskExecutor

}