package hu.mostoha.mobile.android.huki.interactor.exception

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class ExceptionLogger @Inject constructor() {

    private val crashlytics = Firebase.crashlytics

    fun recordException(exception: Exception) {
        crashlytics.recordException(exception)
    }

}
