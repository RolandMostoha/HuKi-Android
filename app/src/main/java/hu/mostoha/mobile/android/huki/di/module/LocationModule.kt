package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.FusedLocationProvider

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class LocationModule {

    @Binds
    abstract fun bindAsyncMyLocationProvider(asyncMyLocationProvider: FusedLocationProvider): AsyncMyLocationProvider

}
