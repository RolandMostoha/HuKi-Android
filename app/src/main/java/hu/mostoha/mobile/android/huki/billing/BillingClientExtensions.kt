package hu.mostoha.mobile.android.huki.billing

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend wrappers around the callback based billing API. The official billing-ktx artifact
 * provides these, but its Kotlin metadata requires a newer Kotlin than the project compiles with,
 * so the plain Java billing artifact is used with these four wrappers instead.
 */

data class ProductDetailsResult(
    val billingResult: BillingResult,
    val productDetailsList: List<ProductDetails>
)

data class PurchasesResult(
    val billingResult: BillingResult,
    val purchasesList: List<Purchase>
)

data class ConsumeResult(
    val billingResult: BillingResult,
    val purchaseToken: String?
)

suspend fun BillingClient.queryProductDetails(params: QueryProductDetailsParams): ProductDetailsResult {
    return suspendCoroutine { continuation ->
        queryProductDetailsAsync(params) { billingResult, result ->
            continuation.resume(ProductDetailsResult(billingResult, result.productDetailsList))
        }
    }
}

suspend fun BillingClient.queryPurchasesAsync(params: QueryPurchasesParams): PurchasesResult {
    return suspendCoroutine { continuation ->
        queryPurchasesAsync(params) { billingResult, purchases ->
            continuation.resume(PurchasesResult(billingResult, purchases))
        }
    }
}

suspend fun BillingClient.acknowledgePurchase(params: AcknowledgePurchaseParams): BillingResult {
    return suspendCoroutine { continuation ->
        acknowledgePurchase(params) { billingResult ->
            continuation.resume(billingResult)
        }
    }
}

suspend fun BillingClient.consumePurchase(params: ConsumeParams): ConsumeResult {
    return suspendCoroutine { continuation ->
        consumeAsync(params) { billingResult, purchaseToken ->
            continuation.resume(ConsumeResult(billingResult, purchaseToken))
        }
    }
}
