package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.extensions.toMillis
import hu.mostoha.mobile.android.huki.provider.DateTimeProvider
import io.mockk.every
import java.time.LocalDate

val DEFAULT_LOCAL_DATE: LocalDate = LocalDate.of(2023, 1, 1)

fun DateTimeProvider.answerDefaults() {
    every { now() } returns DEFAULT_LOCAL_DATE
    every { nowInMillis() } returns DEFAULT_LOCAL_DATE.toMillis()
}
