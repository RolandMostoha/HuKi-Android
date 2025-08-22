package hu.mostoha.mobile.android.huki.logger

import timber.log.Timber

class FakeExceptionLogger : ExceptionLogger {

    override fun recordException(throwable: Throwable) {
        Timber.e(throwable, "[Fake] Recording exception...")
    }

}
