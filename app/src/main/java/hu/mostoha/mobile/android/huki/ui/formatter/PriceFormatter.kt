package hu.mostoha.mobile.android.huki.ui.formatter

object PriceFormatter {

    private const val MICRO_EXCHANGE_RATE = 1_000_000

    fun format(priceInMicros: Long, currencyCode: String): String {
        return "${priceInMicros / MICRO_EXCHANGE_RATE} $currencyCode"
    }

}
