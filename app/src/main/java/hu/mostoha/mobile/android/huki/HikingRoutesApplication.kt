package hu.mostoha.mobile.android.huki

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HikingRoutesApplication : Application() {

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    override fun onCreate() {
        super.onCreate()

        osmConfiguration.init()

        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
