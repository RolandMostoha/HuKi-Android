package hu.mostoha.mobile.android.huki.model.mapper

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.OneTimeBillingProducts
import hu.mostoha.mobile.android.huki.model.domain.OneTimePurchaseRecord
import org.junit.Test

class ProductsUiModelMapperTest {

    private val mapper = ProductsUiModelMapper()

    @Test
    fun `Given known product record, when mapPurchaseHistory, then purchase returns with count`() {
        val records = listOf(OneTimePurchaseRecord(KNOWN_PRODUCT_ID, 2, PURCHASE_TIME))

        val purchases = mapper.mapPurchaseHistory(records)

        assertThat(purchases).hasSize(1)
        assertThat(purchases.first().productType).isEqualTo(OneTimeBillingProducts.LEVEL_1)
        assertThat(purchases.first().count).isEqualTo(2)
    }

    @Test
    fun `Given unknown product record, when mapPurchaseHistory, then record is dropped instead of throwing`() {
        val records = listOf(OneTimePurchaseRecord(UNKNOWN_PRODUCT_ID, 1, PURCHASE_TIME))

        val purchases = mapper.mapPurchaseHistory(records)

        assertThat(purchases).isEmpty()
    }

    @Test
    fun `Given unknown product among known ones, when mapPurchaseHistory, then known purchases still return`() {
        val records = listOf(
            OneTimePurchaseRecord(KNOWN_PRODUCT_ID, 1, PURCHASE_TIME),
            OneTimePurchaseRecord(UNKNOWN_PRODUCT_ID, 1, PURCHASE_TIME),
            OneTimePurchaseRecord(KNOWN_PRODUCT_ID_LEVEL_2, 3, PURCHASE_TIME)
        )

        val purchases = mapper.mapPurchaseHistory(records)

        assertThat(purchases.map { it.productType }).containsExactly(
            OneTimeBillingProducts.LEVEL_1,
            OneTimeBillingProducts.LEVEL_2
        )
    }

    companion object {
        private const val KNOWN_PRODUCT_ID = "huki_support_one_time_level_1"
        private const val KNOWN_PRODUCT_ID_LEVEL_2 = "huki_support_one_time_level_2"
        private const val UNKNOWN_PRODUCT_ID = "huki_support_discontinued_sku"
        private const val PURCHASE_TIME = 1721984400000L
    }

}
