package hu.mostoha.mobile.android.huki.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.osmdroid.tiles.AwsHikingTileUrlProvider
import hu.mostoha.mobile.android.huki.osmdroid.tiles.HikingTileUrlProvider

@Module
@InstallIn(SingletonComponent::class)
class OsmModule {

    @Provides
    fun provideHikingTileUrlProvider(): HikingTileUrlProvider = AwsHikingTileUrlProvider()

}
