package hu.mostoha.mobile.android.huki.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import hu.mostoha.mobile.android.huki.osmdroid.tiles.FakeHikingTileUrlProvider
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [OsmModule::class]
)
class TestOsmModule {

    @Provides
    fun provideHikingTileUrlProvider(): HikingTileUrlProvider = FakeHikingTileUrlProvider

}
