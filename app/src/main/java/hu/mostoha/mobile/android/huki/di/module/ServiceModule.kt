package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import hu.mostoha.mobile.android.huki.executor.DefaultTaskExecutor
import hu.mostoha.mobile.android.huki.executor.TaskExecutor

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindTaskExecutor(taskExecutor: DefaultTaskExecutor): TaskExecutor

}