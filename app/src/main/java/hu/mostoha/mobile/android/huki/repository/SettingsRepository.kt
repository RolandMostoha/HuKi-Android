package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.android.huki.model.domain.BaseLayer
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.util.EnumUtil.valueOf
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_SCALE_FACTOR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun saveBaseLayer(baseLayer: BaseLayer) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.Settings.BASE_LAYER] = baseLayer.name
        }
    }

    fun getBaseLayer(): Flow<BaseLayer> {
        return dataStore.data
            .map { preferences ->
                val baseLayerEnum = preferences[DataStoreConstants.Settings.BASE_LAYER]

                BaseLayer.entries.valueOf(baseLayerEnum) ?: BaseLayer.MAPNIK
            }
    }

    fun getMapScaleFactor(): Flow<Double> {
        return dataStore.data
            .map { preferences ->
                preferences[DataStoreConstants.Settings.MAP_SCALE_FACTOR] ?: MAP_DEFAULT_SCALE_FACTOR
            }
    }

    suspend fun saveMapScaleFactor(mapScaleFactor: Double) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.Settings.MAP_SCALE_FACTOR] = mapScaleFactor
        }
    }

    fun getTheme(): Flow<Theme> {
        return dataStore.data
            .map { preferences ->
                val ordinal = preferences[DataStoreConstants.Settings.THEME] ?: return@map Theme.SYSTEM

                return@map Theme.entries[ordinal]
            }
    }

    suspend fun saveTheme(theme: Theme) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.Settings.THEME] = theme.ordinal
        }
    }

    fun isGpxSlopeColoringEnabled(): Flow<Boolean> {
        return dataStore.data
            .map { preferences ->
                preferences[DataStoreConstants.Settings.GPX_SLOPE_COLORING_ENABLED] ?: true
            }
    }

    suspend fun saveGpxSlopeColoringEnabled(enabled: Boolean) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.Settings.GPX_SLOPE_COLORING_ENABLED] = enabled
        }
    }

}
