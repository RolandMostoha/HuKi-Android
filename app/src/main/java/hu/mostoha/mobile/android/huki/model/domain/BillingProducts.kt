package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.android.billingclient.api.BillingClient
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage

sealed interface BillingProductType {
    val productId: String
    val productType: String
    val productName: Message.Res
    val productMessage: Message.Res
    val productIcon: Int
    val productColorRes: Int
}

enum class OneTimeBillingProducts(
    override val productId: String,
    override val productType: String,
    override val productName: Message.Res,
    override val productMessage: Message.Res,
    @DrawableRes override val productIcon: Int,
    @ColorRes override val productColorRes: Int
) : BillingProductType {
    LEVEL_1(
        productId = "huki_support_one_time_level_1",
        productType = BillingClient.ProductType.INAPP,
        productName = R.string.support_product_name_one_time_level_1.toMessage(),
        productMessage = R.string.support_supporter_message_one_time_level_1.toMessage(),
        productIcon = R.drawable.ic_product_owl,
        productColorRes = R.color.colorPaymentsOwlIcon
    ),
    LEVEL_2(
        productId = "huki_support_one_time_level_2",
        productType = BillingClient.ProductType.INAPP,
        productName = R.string.support_product_name_one_time_level_2.toMessage(),
        productMessage = R.string.support_supporter_message_one_time_level_2.toMessage(),
        productIcon = R.drawable.ic_product_squirrel,
        productColorRes = R.color.colorPaymentsSquirrelIcon
    ),
}

enum class RecurringBillingProducts(
    override val productId: String,
    override val productType: String,
    override val productName: Message.Res,
    override val productMessage: Message.Res,
    @DrawableRes override val productIcon: Int,
    @ColorRes override val productColorRes: Int,
) : BillingProductType {
    LEVEL_1(
        productId = "huki_support_recurring_level_1",
        productType = BillingClient.ProductType.SUBS,
        productName = R.string.support_product_name_recurring_level_1.toMessage(),
        productMessage = R.string.support_supporter_message_recurring_level_1.toMessage(),
        productIcon = R.drawable.ic_product_boar,
        productColorRes = R.color.colorPaymentsBoarIcon
    ),
    LEVEL_2(
        productId = "huki_support_recurring_level_2",
        productType = BillingClient.ProductType.SUBS,
        productName = R.string.support_product_name_recurring_level_2.toMessage(),
        productMessage = R.string.support_supporter_message_recurring_level_2.toMessage(),
        productIcon = R.drawable.ic_product_deer,
        productColorRes = R.color.colorPaymentsDeerIcon
    ),
}

fun String.toOneTimeBillingProduct(): OneTimeBillingProducts {
    return OneTimeBillingProducts.entries.first { it.productId == this }
}

fun String.toRecurringBillingProduct(): RecurringBillingProducts {
    return RecurringBillingProducts.entries.first { it.productId == this }
}

fun String.toBillingProduct(): BillingProductType {
    val oneTime = OneTimeBillingProducts.entries.firstOrNull { it.productId == this }

    if (oneTime != null) return oneTime

    val recurring = RecurringBillingProducts.entries.firstOrNull { it.productId == this }

    if (recurring != null) return recurring

    throw IllegalArgumentException("Invalid product id: $this")
}

fun OneTimeBillingProducts.isOneTime(): Boolean = this.productType == BillingClient.ProductType.INAPP

fun OneTimeBillingProducts.isRecurring(): Boolean = this.productType == BillingClient.ProductType.SUBS

fun String.isOneTime(): Boolean = OneTimeBillingProducts.entries.any { it.productId == this }

fun String.isRecurring(): Boolean = RecurringBillingProducts.entries.any { it.productId == this }
