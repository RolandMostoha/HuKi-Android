package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    fun getMapScaleFactor(): Flow<Float> {
        return dataStore.data
            .map { preferences ->
                preferences[DataStoreConstants.Settings.MAP_SCALE_FACTOR] ?: MAP_DEFAULT_SCALE_FACTOR
            }
    }

    suspend fun saveMapScaleFactor(mapScaleFactor: Float) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.Settings.MAP_SCALE_FACTOR] = mapScaleFactor
        }
    }

}
