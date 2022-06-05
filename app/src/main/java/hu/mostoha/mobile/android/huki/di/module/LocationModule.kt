package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import hu.mostoha.mobile.android.huki.osmdroid.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.FusedLocationProvider

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class LocationModule {

    @Binds
    abstract fun bindAsyncMyLocationProvider(taskExecutor: FusedLocationProvider): AsyncMyLocationProvider

}
