package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.android.huki.model.domain.GraphhopperLimitState
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GraphhopperLimitRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun getLimitState(nowMillis: Long): GraphhopperLimitState {
        val preferences = dataStore.data.first()
        val reserveReachedUntil = preferences[DataStoreConstants.RoutePlanner.GRAPHHOPPER_RESERVE_REACHED_UNTIL] ?: 0L
        val blockedUntil = preferences[DataStoreConstants.RoutePlanner.GRAPHHOPPER_BLOCKED_UNTIL] ?: 0L

        return GraphhopperLimitState(
            isReserveReached = nowMillis < reserveReachedUntil,
            isBlocked = nowMillis < blockedUntil,
        )
    }

    suspend fun setReserveReachedUntil(untilMillis: Long) {
        dataStore.edit { it[DataStoreConstants.RoutePlanner.GRAPHHOPPER_RESERVE_REACHED_UNTIL] = untilMillis }
    }

    suspend fun setBlockedUntil(untilMillis: Long) {
        dataStore.edit { it[DataStoreConstants.RoutePlanner.GRAPHHOPPER_BLOCKED_UNTIL] = untilMillis }
    }

}
