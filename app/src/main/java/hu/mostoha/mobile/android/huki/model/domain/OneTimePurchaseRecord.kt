package hu.mostoha.mobile.android.huki.model.domain

data class OneTimePurchaseRecord(
    val productId: String,
    val count: Int,
    val lastPurchaseTimeMillis: Long,
)
