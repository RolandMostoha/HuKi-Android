package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.model.domain.BillingProductType
import java.time.LocalDateTime

data class ProductsUiModel(
    val products: List<BillingProduct> = emptyList(),
    val purchases: List<BillingPurchase> = emptyList(),
    val isLoading: Boolean = true,
    val error: Message.Res? = null,
)

data class BillingPurchase(
    val productType: BillingProductType,
    val purchaseTime: LocalDateTime,
    val purchaseToken: String,
)
