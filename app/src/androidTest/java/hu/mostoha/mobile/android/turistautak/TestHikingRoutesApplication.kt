package hu.mostoha.mobile.android.turistautak

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication
import timber.log.Timber

@CustomTestApplication(TestHikingRoutesApplication::class)
interface HiltTestApplication

open class TestHikingRoutesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }

}
