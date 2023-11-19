package hu.mostoha.mobile.android.huki.extensions

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_DATE_TIME_FORMAT = "yyyy.MM.dd HH:mm"
private const val DEFAULT_DATE_FORMAT = "yyyy.MM.dd"
private const val WEEK_DAY_COUNT = 7L

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun LocalDate.toMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun LocalDateTime.formatLongDateTime(): String {
    return this.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT))
}

fun LocalDate.formatFriendlyDate(today: LocalDate): Message {
    return when {
        this == today -> R.string.default_date_today.toMessage()
        this == today.minusDays(1) -> R.string.default_date_yesterday.toMessage()
        this.isAfter(today.minusDays(WEEK_DAY_COUNT)) -> when (today.dayOfWeek!!) {
            DayOfWeek.MONDAY -> R.string.default_date_monday
            DayOfWeek.TUESDAY -> R.string.default_date_tuesday
            DayOfWeek.WEDNESDAY -> R.string.default_date_wednesday
            DayOfWeek.THURSDAY -> R.string.default_date_thursday
            DayOfWeek.FRIDAY -> R.string.default_date_friday
            DayOfWeek.SATURDAY -> R.string.default_date_saturday
            DayOfWeek.SUNDAY -> R.string.default_date_sunday
        }.toMessage()
        else -> Message.Text(this.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
    }
}
