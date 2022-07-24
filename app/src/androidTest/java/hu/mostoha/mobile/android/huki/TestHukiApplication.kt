package hu.mostoha.mobile.android.huki

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication
import timber.log.Timber

@CustomTestApplication(TestHukiApplication::class)
interface HiltTestApplication

open class TestHukiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }

}
