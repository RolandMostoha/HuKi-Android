package hu.mostoha.mobile.android.huki.model.mapper

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.extensions.toLocalDateTime
import hu.mostoha.mobile.android.huki.model.domain.toBillingProduct
import hu.mostoha.mobile.android.huki.model.domain.toOneTimeBillingProduct
import hu.mostoha.mobile.android.huki.model.domain.toRecurringBillingProduct
import hu.mostoha.mobile.android.huki.model.ui.BillingProduct
import hu.mostoha.mobile.android.huki.model.ui.BillingPurchase
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.ui.formatter.PriceFormatter
import javax.inject.Inject

class ProductsUiModelMapper @Inject constructor() {

    fun mapOneTimeProducts(products: List<ProductDetails>): List<BillingProduct> {
        return products.map { productDetails ->

            val billingProduct = productDetails.productId.toOneTimeBillingProduct()
            val details = productDetails.oneTimePurchaseOfferDetails!!

            BillingProduct(
                productDetails = productDetails,
                type = billingProduct,
                priceText = PriceFormatter
                    .format(details.priceAmountMicros, details.priceCurrencyCode)
                    .toMessage()
            )
        }
    }

    fun mapRecurringProducts(products: List<ProductDetails>): List<BillingProduct> {
        return products.map { productDetails ->
            val billingProduct = productDetails.productId.toRecurringBillingProduct()
            val details = productDetails.subscriptionOfferDetails!!
            val pricingPhase = details.first().pricingPhases.pricingPhaseList.first()

            BillingProduct(
                productDetails = productDetails,
                type = billingProduct,
                priceText = Message.Res(
                    res = R.string.support_recurring_payment_month_template,
                    formatArgs = listOf(
                        PriceFormatter.format(
                            pricingPhase.priceAmountMicros,
                            pricingPhase.priceCurrencyCode
                        )
                    )
                )
            )
        }
    }

    fun mapHistoryPurchases(purchases: List<PurchaseHistoryRecord>): List<BillingPurchase> {
        return purchases.map { purchase ->
            BillingPurchase(
                productType = purchase.products.first().toBillingProduct(),
                purchaseTime = purchase.purchaseTime.toLocalDateTime(),
                purchaseToken = purchase.purchaseToken
            )
        }
    }

    fun mapPurchases(purchases: List<Purchase>): List<BillingPurchase> {
        return purchases.map { purchase ->
            BillingPurchase(
                productType = purchase.products.first().toBillingProduct(),
                purchaseTime = purchase.purchaseTime.toLocalDateTime(),
                purchaseToken = purchase.purchaseToken
            )
        }
    }

}
