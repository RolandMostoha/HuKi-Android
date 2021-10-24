package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLayerRepository(layerRepository: DefaultHikingLayerRepository): HikingLayerRepository

    @Singleton
    @Binds
    abstract fun bindPlacesRepository(placesRepositoryImpl: OsmPlacesRepository): PlacesRepository

    @Singleton
    @Binds
    abstract fun bindLandscapeRepository(landscapeRepository: LocalLandscapeRepository): LandscapeRepository

}
