package hu.mostoha.mobile.android.huki.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun LocalDateTime.toLongFormat(): String {
    return this.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
}
