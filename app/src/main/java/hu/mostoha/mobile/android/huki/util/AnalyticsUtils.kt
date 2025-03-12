package hu.mostoha.mobile.android.huki.util

fun ClosedRange<*>.toAnalyticsEvent(): String {
    return "${this.start}_${this.endInclusive}"
}

inline fun <reified T : Comparable<T>> List<ClosedRange<T>>.toAnalyticsEvent(value: T): String? {
    return this.firstOrNull { range ->
        value in range
    }?.toAnalyticsEvent()
}
