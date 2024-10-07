package hu.mostoha.mobile.android.huki.model.ui

import com.android.billingclient.api.ProductDetails
import hu.mostoha.mobile.android.huki.model.domain.BillingProductType

data class BillingProduct(
    val productDetails: ProductDetails,
    val type: BillingProductType,
    val priceText: Message,
)
