package hu.mostoha.mobile.android.huki.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import timber.log.Timber
import java.lang.reflect.Field
import javax.inject.Inject

class BillingResponseHandler @Inject constructor(
    private val analyticsService: AnalyticsService
) {

    fun handleBillingResponse(
        billingAction: BillingAction,
        billingResult: BillingResult,
        onSuccess: ((BillingResult) -> Unit)? = null,
        onError: ((BillingResult) -> Unit)? = null
    ) {
        val responseCode = billingResult.responseCode
        val responseCodeName = responseCode.fieldName()

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Timber.d("Billing: success $billingAction")

                onSuccess?.invoke(billingResult)
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Timber.d("Billing: user canceled $billingAction")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Timber.d("Billing: item already owned $billingAction")
            }
            else -> {
                Timber.e(
                    "Billing: error ${
                        listOfNotNull(
                            billingAction,
                            responseCodeName,
                            billingResult.debugMessage.ifEmpty { null },
                        ).joinToString(", ")
                    }"
                )

                onError?.invoke(billingResult)
            }
        }

        analyticsService.billingEvent(billingAction, responseCode)
    }

}

fun Int.fieldName(): String? {
    val fields: Array<Field> = BillingClient.BillingResponseCode::class.java.fields

    for (field in fields) {
        if (field.getInt(null) == this) {
            return field.name
        }
    }

    return null
}
