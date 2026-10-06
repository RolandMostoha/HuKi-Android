package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.GraphhopperLimitState
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class GraphhopperLimitRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: GraphhopperLimitRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Before
    fun init() {
        hiltRule.inject()

        runTest {
            dataStore.edit { preferences ->
                preferences.remove(DataStoreConstants.RoutePlanner.GRAPHHOPPER_RESERVE_REACHED_UNTIL)
                preferences.remove(DataStoreConstants.RoutePlanner.GRAPHHOPPER_BLOCKED_UNTIL)
            }
        }
    }

    @Test
    fun givenEmptyStore_whenGetLimitState_thenNoLimitReturns() {
        runTest {
            val limitState = repository.getLimitState(NOW_MILLIS)

            assertThat(limitState).isEqualTo(GraphhopperLimitState(isReserveReached = false, isBlocked = false))
        }
    }

    @Test
    fun givenLimitsUntilFuture_whenGetLimitState_thenLimitsReturn() {
        runTest {
            repository.setReserveReachedUntil(NOW_MILLIS + 1)
            repository.setBlockedUntil(NOW_MILLIS + 1)

            val limitState = repository.getLimitState(NOW_MILLIS)

            assertThat(limitState).isEqualTo(GraphhopperLimitState(isReserveReached = true, isBlocked = true))
        }
    }

    @Test
    fun givenLimitsExpired_whenGetLimitState_thenNoLimitReturns() {
        runTest {
            repository.setReserveReachedUntil(NOW_MILLIS)
            repository.setBlockedUntil(NOW_MILLIS)

            val limitState = repository.getLimitState(NOW_MILLIS)

            assertThat(limitState).isEqualTo(GraphhopperLimitState(isReserveReached = false, isBlocked = false))
        }
    }

    companion object {
        private const val NOW_MILLIS = 1_791_640_800_000L
    }

}
