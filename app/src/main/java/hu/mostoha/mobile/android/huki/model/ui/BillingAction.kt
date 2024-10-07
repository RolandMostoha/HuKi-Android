package hu.mostoha.mobile.android.huki.model.ui

import hu.mostoha.mobile.android.huki.R

enum class BillingAction {
    START_CONNECTION,
    QUERY_PRODUCTS,
    QUERY_PURCHASES,
    PURCHASES_UPDATED,
    LAUNCH_BILLING_FLOW,
    CONSUME_PURCHASE,
    ACKNOWLEDGE_PURCHASE,
}

fun BillingAction.toMessage(): Message.Res {
    return when (this) {
        BillingAction.START_CONNECTION -> R.string.support_error_start_connection.toMessage()
        BillingAction.QUERY_PRODUCTS -> R.string.support_error_query_products.toMessage()
        BillingAction.QUERY_PURCHASES -> R.string.support_error_query_purchases.toMessage()
        BillingAction.PURCHASES_UPDATED -> R.string.support_error_purchases_updated.toMessage()
        BillingAction.LAUNCH_BILLING_FLOW -> R.string.support_error_launch_billing_flow.toMessage()
        BillingAction.CONSUME_PURCHASE -> R.string.support_error_consume_purchase.toMessage()
        BillingAction.ACKNOWLEDGE_PURCHASE -> R.string.support_error_acknowledge_purchase.toMessage()
    }
}
