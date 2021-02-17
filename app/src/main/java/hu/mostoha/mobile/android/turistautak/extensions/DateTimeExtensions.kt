package hu.mostoha.mobile.android.turistautak.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun LocalDateTime.formatShortDate(): String {
    return toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
}
