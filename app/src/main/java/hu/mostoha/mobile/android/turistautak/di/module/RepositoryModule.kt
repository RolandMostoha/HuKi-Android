package hu.mostoha.mobile.android.turistautak.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import hu.mostoha.mobile.android.turistautak.repository.LayerRepository
import hu.mostoha.mobile.android.turistautak.repository.LayerRepositoryImpl
import hu.mostoha.mobile.android.turistautak.repository.OverpassRepository
import hu.mostoha.mobile.android.turistautak.repository.OverpassRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindLayerRepository(fileLayerRepository: LayerRepositoryImpl): LayerRepository

    @Singleton
    @Binds
    abstract fun bindOverpassRepository(overpassRepositoryImpl: OverpassRepositoryImpl): OverpassRepository

}