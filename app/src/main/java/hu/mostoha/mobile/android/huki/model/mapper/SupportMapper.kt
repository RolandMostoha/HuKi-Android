package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.model.domain.OneTimePurchaseRecord

private const val ENTRY_SEPARATOR = ";"

fun String.toOneTimePurchaseRecord(): OneTimePurchaseRecord? {
    val parts = split(ENTRY_SEPARATOR)

    val productId = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
    val count = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val lastPurchaseTimeMillis = parts.getOrNull(2)?.toLongOrNull()?.takeIf { it >= 0 } ?: return null

    return OneTimePurchaseRecord(productId, count, lastPurchaseTimeMillis)
}

fun Set<String>.toOneTimePurchaseRecords(): List<OneTimePurchaseRecord> {
    return mapNotNull { it.toOneTimePurchaseRecord() }
}

fun OneTimePurchaseRecord.toDataStoreEntry(): String {
    return listOf(productId, count, lastPurchaseTimeMillis).joinToString(ENTRY_SEPARATOR)
}

fun List<OneTimePurchaseRecord>.toDataStoreEntries(): Set<String> {
    return map { it.toDataStoreEntry() }.toSet()
}
