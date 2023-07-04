package hu.mostoha.mobile.android.huki.logger

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class FirebaseExceptionLogger @Inject constructor() : ExceptionLogger {

    private val crashlytics = Firebase.crashlytics

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

}
