package hu.mostoha.mobile.android.huki.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.mostoha.mobile.android.huki.database.HukiDatabase
import hu.mostoha.mobile.android.huki.database.PlaceHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    companion object {
        const val DATABASE_NAME = "huki-database"
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): HukiDatabase {
        return Room.databaseBuilder(
            context,
            HukiDatabase::class.java,
            DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePlaceHistoryDao(database: HukiDatabase): PlaceHistoryDao = database.placeHistoryDao()

}
