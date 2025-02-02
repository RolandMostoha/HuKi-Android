package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MapConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun saveBoundingBox(boundingBox: BoundingBox) {
        dataStore.edit { settings ->
            settings[DataStoreConstants.MapConfig.BOUNDING_BOX_NORTH] = boundingBox.north
            settings[DataStoreConstants.MapConfig.BOUNDING_BOX_EAST] = boundingBox.east
            settings[DataStoreConstants.MapConfig.BOUNDING_BOX_SOUTH] = boundingBox.south
            settings[DataStoreConstants.MapConfig.BOUNDING_BOX_WEST] = boundingBox.west
        }
    }

    suspend fun getBoundingBox(): BoundingBox? {
        return dataStore.data
            .map { preferences ->
                val north = preferences[DataStoreConstants.MapConfig.BOUNDING_BOX_NORTH] ?: return@map null
                val east = preferences[DataStoreConstants.MapConfig.BOUNDING_BOX_EAST] ?: return@map null
                val south = preferences[DataStoreConstants.MapConfig.BOUNDING_BOX_SOUTH] ?: return@map null
                val west = preferences[DataStoreConstants.MapConfig.BOUNDING_BOX_WEST] ?: return@map null

                BoundingBox(north, east, south, west)
            }
            .firstOrNull()
    }

}
