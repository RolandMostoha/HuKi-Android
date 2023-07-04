package hu.mostoha.mobile.android.huki.logger

class FakeExceptionLogger : ExceptionLogger {

    override fun recordException(throwable: Throwable) = Unit

}
