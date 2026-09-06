package hu.mostoha.mobile.android.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import hu.mostoha.mobile.android.huki.model.domain.OneTimePurchaseRecord
import hu.mostoha.mobile.android.huki.model.mapper.toDataStoreEntries
import hu.mostoha.mobile.android.huki.model.mapper.toOneTimePurchaseRecords
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Play Billing only reports currently owned (non-consumed) purchases, but one-time support
 * purchases are consumed right away so they can be re-purchased. This repository is the
 * durable record of "has ever supported HuKi" - and how many times - that survives consumption.
 */
class SupportRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    /**
     * Idempotent per purchase token: the same purchase can reach here more than once - a failed
     * consume leaves it owned and the next sweep picks it up again - but must count only once.
     */
    suspend fun recordOneTimePurchase(productId: String, purchaseToken: String, purchaseTimeMillis: Long) {
        dataStore.edit { preferences ->
            if (!preferences.markPurchaseTokenRecorded(purchaseToken)) return@edit

            val existing = preferences.readPurchaseRecords()
            val previousCount = existing.firstOrNull { it.productId == productId }?.count ?: 0
            val updated = OneTimePurchaseRecord(
                productId = productId,
                count = previousCount + 1,
                lastPurchaseTimeMillis = purchaseTimeMillis,
            )

            preferences.writePurchaseRecords(existing.filterNot { it.productId == productId } + updated)
        }
    }

    fun getOneTimePurchaseHistory(): Flow<List<OneTimePurchaseRecord>> {
        return dataStore.data.map { preferences -> preferences.readPurchaseRecords() }
    }

    private fun MutablePreferences.markPurchaseTokenRecorded(purchaseToken: String): Boolean {
        val tokens = this[DataStoreConstants.Support.RECORDED_PURCHASE_TOKENS].orEmpty()

        if (purchaseToken in tokens) return false

        this[DataStoreConstants.Support.RECORDED_PURCHASE_TOKENS] = tokens + purchaseToken

        return true
    }

    private fun Preferences.readPurchaseRecords(): List<OneTimePurchaseRecord> {
        return this[DataStoreConstants.Support.PURCHASED_ONE_TIME_PRODUCTS]
            .orEmpty()
            .toOneTimePurchaseRecords()
    }

    private fun MutablePreferences.writePurchaseRecords(records: List<OneTimePurchaseRecord>) {
        this[DataStoreConstants.Support.PURCHASED_ONE_TIME_PRODUCTS] = records.toDataStoreEntries()
    }

}
