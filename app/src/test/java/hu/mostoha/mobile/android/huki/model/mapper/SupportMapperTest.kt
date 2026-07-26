package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.OneTimePurchaseRecord
import org.junit.Test

class SupportMapperTest {

    @Test
    fun `Given valid entry, when toOneTimePurchaseRecord, then record returns`() {
        val entry = "$PRODUCT_ID;2;$PURCHASE_TIME"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isEqualTo(
            OneTimePurchaseRecord(
                productId = PRODUCT_ID,
                count = 2,
                lastPurchaseTimeMillis = PURCHASE_TIME
            )
        )
    }

    @Test
    fun `Given entry with missing purchase time, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;2"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with non numeric count, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;many;$PURCHASE_TIME"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with non numeric purchase time, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;2;yesterday"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given blank entry, when toOneTimePurchaseRecord, then null returns`() {
        val entry = ""

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with blank product id, when toOneTimePurchaseRecord, then null returns`() {
        val entry = ";2;$PURCHASE_TIME"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with zero count, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;0;$PURCHASE_TIME"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with negative count, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;-2;$PURCHASE_TIME"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entry with negative purchase time, when toOneTimePurchaseRecord, then null returns`() {
        val entry = "$PRODUCT_ID;2;-1"

        val record = entry.toOneTimePurchaseRecord()

        assertThat(record).isNull()
    }

    @Test
    fun `Given entries with a corrupt one, when toOneTimePurchaseRecords, then only valid records return`() {
        val entries = setOf(
            "$PRODUCT_ID;2;$PURCHASE_TIME",
            "corrupt-entry",
            "$PRODUCT_ID_LEVEL_2;1;$PURCHASE_TIME"
        )

        val records = entries.toOneTimePurchaseRecords()

        assertThat(records).containsExactly(
            OneTimePurchaseRecord(PRODUCT_ID, 2, PURCHASE_TIME),
            OneTimePurchaseRecord(PRODUCT_ID_LEVEL_2, 1, PURCHASE_TIME)
        )
    }

    @Test
    fun `Given empty entries, when toOneTimePurchaseRecords, then empty list returns`() {
        val entries = emptySet<String>()

        val records = entries.toOneTimePurchaseRecords()

        assertThat(records).isEmpty()
    }

    @Test
    fun `Given record, when toDataStoreEntry, then serialized entry returns`() {
        val record = OneTimePurchaseRecord(PRODUCT_ID, 3, PURCHASE_TIME)

        val entry = record.toDataStoreEntry()

        assertThat(entry).isEqualTo("$PRODUCT_ID;3;$PURCHASE_TIME")
    }

    @Test
    fun `Given records, when toDataStoreEntries, then serialized entries return`() {
        val records = listOf(
            OneTimePurchaseRecord(PRODUCT_ID, 2, PURCHASE_TIME),
            OneTimePurchaseRecord(PRODUCT_ID_LEVEL_2, 1, PURCHASE_TIME)
        )

        val entries = records.toDataStoreEntries()

        assertThat(entries).containsExactly(
            "$PRODUCT_ID;2;$PURCHASE_TIME",
            "$PRODUCT_ID_LEVEL_2;1;$PURCHASE_TIME"
        )
    }

    @Test
    fun `Given record, when serialized and deserialized, then original record returns`() {
        val record = OneTimePurchaseRecord(PRODUCT_ID, 5, PURCHASE_TIME)

        val restored = record.toDataStoreEntry().toOneTimePurchaseRecord()

        assertThat(restored).isEqualTo(record)
    }

    companion object {
        private const val PRODUCT_ID = "huki_support_one_time_level_1"
        private const val PRODUCT_ID_LEVEL_2 = "huki_support_one_time_level_2"
        private const val PURCHASE_TIME = 1721984400000L
    }

}
