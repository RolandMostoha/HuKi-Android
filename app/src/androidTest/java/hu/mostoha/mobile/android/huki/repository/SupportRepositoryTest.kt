package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import hu.mostoha.mobile.android.huki.model.domain.OneTimePurchaseRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@MediumTest
@HiltAndroidTest
class SupportRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: SupportRepository

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Before
    fun init() {
        hiltRule.inject()

        runTest {
            dataStore.edit { preferences ->
                preferences.remove(DataStoreConstants.Support.PURCHASED_ONE_TIME_PRODUCTS)
                preferences.remove(DataStoreConstants.Support.RECORDED_PURCHASE_TOKENS)
                preferences.remove(DataStoreConstants.Support.LEGACY_HISTORY_MIGRATED)
            }
        }
    }

    @Test
    fun givenEmptyStore_whenRecordOneTimePurchase_thenRecordWithCountOneReturns() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME))
        }
    }

    @Test
    fun givenRecordedPurchase_whenRecordSameTokenAgain_thenCountIsNotIncremented() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME))
        }
    }

    @Test
    fun givenRecordedPurchase_whenRecordNewToken_thenCountIsIncremented() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_2, PURCHASE_TIME_LATER)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 2, PURCHASE_TIME_LATER))
        }
    }

    @Test
    fun givenPurchasesOfTwoProducts_whenRecordOneTimePurchase_thenCountsAreTrackedSeparately() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_2, PURCHASE_TIME_LATER)
            repository.recordOneTimePurchase(PRODUCT_ID_LEVEL_2, TOKEN_3, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(
                OneTimePurchaseRecord(PRODUCT_ID, 2, PURCHASE_TIME_LATER),
                OneTimePurchaseRecord(PRODUCT_ID_LEVEL_2, 1, PURCHASE_TIME)
            )
        }
    }

    @Test
    fun givenEmptyStore_whenRecordLegacyPurchase_thenProductIsSeededWithCountOne() {
        runTest {
            val isSeeded = repository.recordLegacyPurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(isSeeded).isTrue()
            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME))
        }
    }

    @Test
    fun givenTrackedProduct_whenRecordLegacyPurchase_thenCountIsNotTouched() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_2, PURCHASE_TIME_LATER)

            val isSeeded = repository.recordLegacyPurchase(PRODUCT_ID, TOKEN_3, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(isSeeded).isFalse()
            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 2, PURCHASE_TIME_LATER))
        }
    }

    /**
     * The legacy backfill and the consume sweep see the same still-owned purchase: seeded once by
     * the migration, then swept - the shared token must keep it at a single count.
     */
    @Test
    fun givenLegacyPurchaseStillOwned_whenSweptWithSameToken_thenCountStaysOne() {
        runTest {
            repository.recordLegacyPurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME))
        }
    }

    /**
     * The legacy backfill must claim the token even when it doesn't seed the count, otherwise the
     * sweep counts an already tracked - but still owned - legacy purchase a second time.
     */
    @Test
    fun givenTrackedProduct_whenLegacyPurchaseIsSweptWithItsOwnToken_thenCountStaysOne() {
        runTest {
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            repository.recordLegacyPurchase(PRODUCT_ID, TOKEN_2, PURCHASE_TIME)
            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_2, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME))
        }
    }

    @Test
    fun givenCorruptEntryInStore_whenRecordOneTimePurchase_thenValidRecordsSurvive() {
        runTest {
            dataStore.edit { preferences ->
                preferences[DataStoreConstants.Support.PURCHASED_ONE_TIME_PRODUCTS] = setOf(
                    "corrupt-entry",
                    "$PRODUCT_ID_LEVEL_2;1;$PURCHASE_TIME"
                )
            }

            repository.recordOneTimePurchase(PRODUCT_ID, TOKEN_1, PURCHASE_TIME)

            val history = repository.getOneTimePurchaseHistory().first()

            assertThat(history).containsExactly(
                OneTimePurchaseRecord(PRODUCT_ID_LEVEL_2, 1, PURCHASE_TIME),
                OneTimePurchaseRecord(PRODUCT_ID, 1, PURCHASE_TIME)
            )
        }
    }

    @Test
    fun givenFreshInstall_whenIsLegacyHistoryMigrated_thenFalseReturns() {
        runTest {
            assertThat(repository.isLegacyHistoryMigrated()).isFalse()
        }
    }

    @Test
    fun givenMigrationFlagSet_whenIsLegacyHistoryMigrated_thenTrueReturns() {
        runTest {
            repository.setLegacyHistoryMigrated()

            assertThat(repository.isLegacyHistoryMigrated()).isTrue()
        }
    }

    companion object {
        private const val PRODUCT_ID = "huki_support_one_time_level_1"
        private const val PRODUCT_ID_LEVEL_2 = "huki_support_one_time_level_2"
        private const val TOKEN_1 = "purchase-token-1"
        private const val TOKEN_2 = "purchase-token-2"
        private const val TOKEN_3 = "purchase-token-3"
        private const val PURCHASE_TIME = 1721984400000L
        private const val PURCHASE_TIME_LATER = 1722984400000L
    }

}
