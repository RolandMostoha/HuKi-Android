package hu.mostoha.mobile.android.huki.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.repository.VersionConfiguration
import hu.mostoha.mobile.android.huki.repository.VersionDataStoreConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VersionConfigurationModule {

    @Singleton
    @Binds
    abstract fun bindVersionConfiguration(versionConfiguration: VersionDataStoreConfiguration): VersionConfiguration

}
