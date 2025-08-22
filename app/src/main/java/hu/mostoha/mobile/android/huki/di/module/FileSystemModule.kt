package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.repository.DefaultGpxRepository
import hu.mostoha.mobile.android.huki.repository.DefaultLayersRepository
import hu.mostoha.mobile.android.huki.repository.GpxRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileSystemModule {

    @Singleton
    @Binds
    abstract fun bindLayersRepository(layerRepository: DefaultLayersRepository): LayersRepository

    @Singleton
    @Binds
    abstract fun bindGpxRepository(gpxRepository: DefaultGpxRepository): GpxRepository

}
