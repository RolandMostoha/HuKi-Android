package hu.mostoha.mobile.android.huki.logger

interface ExceptionLogger {

    fun recordException(throwable: Throwable)

}
