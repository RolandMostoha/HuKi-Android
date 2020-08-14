package hu.mostoha.mobile.android.turistautak.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hu.mostoha.mobile.android.turistautak.repository.*
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLayerRepository(layerRepository: HikingLayerRepositoryImpl): HikingLayerRepository

    @Singleton
    @Binds
    abstract fun bindOverpassRepository(overpassRepositoryImpl: OverpassRepositoryImpl): OverpassRepository

    @Singleton
    @Binds
    abstract fun bindPlacesRepository(placesRepositoryImpl: OsmPlacesRepository): PlacesRepository

    @Singleton
    @Binds
    abstract fun bindLandscapeRepository(landscapeRepository: LocalLandscapeRepository): LandscapeRepository

}