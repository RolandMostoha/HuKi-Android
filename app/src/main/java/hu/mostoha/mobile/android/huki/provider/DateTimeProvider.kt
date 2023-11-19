package hu.mostoha.mobile.android.huki.provider

import java.time.LocalDate
import javax.inject.Inject

class DateTimeProvider @Inject constructor() {

    fun now(): LocalDate = LocalDate.now()

    fun nowInMillis(): Long = System.currentTimeMillis()

}
