package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.repository.FileBasedLayersRepository
import hu.mostoha.mobile.android.huki.repository.GeocodingRepository
import hu.mostoha.mobile.android.huki.repository.LandscapeRepository
import hu.mostoha.mobile.android.huki.repository.LayersRepository
import hu.mostoha.mobile.android.huki.repository.LocalLandscapeRepository
import hu.mostoha.mobile.android.huki.repository.OsmPlacesRepository
import hu.mostoha.mobile.android.huki.repository.PhotonGeocodingRepository
import hu.mostoha.mobile.android.huki.repository.PlacesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLayersRepository(layerRepository: FileBasedLayersRepository): LayersRepository

    @Singleton
    @Binds
    abstract fun bindPlacesRepository(placesRepository: OsmPlacesRepository): PlacesRepository

    @Singleton
    @Binds
    abstract fun bindGeocodingRepository(geocodingRepository: PhotonGeocodingRepository): GeocodingRepository

    @Singleton
    @Binds
    abstract fun bindLandscapeRepository(landscapeRepository: LocalLandscapeRepository): LandscapeRepository

}
