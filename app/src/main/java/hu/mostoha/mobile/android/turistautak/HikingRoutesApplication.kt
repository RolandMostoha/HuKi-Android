package hu.mostoha.mobile.android.turistautak

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import org.osmdroid.config.Configuration
import timber.log.Timber

@HiltAndroidApp
class HikingRoutesApplication : Application() {

    companion object {
        const val KEY_GLOBAL_SHARED_PREFERENCES = "KEY_GLOBAL_SHARED_PREFERENCES"
    }

    override fun onCreate() {
        super.onCreate()

        initOsmDroid()
        initTimber()
    }

    private fun initOsmDroid() {
        Configuration.getInstance().apply {
            if (BuildConfig.DEBUG) {
                isDebugMapView = true
                isDebugMode = true
                isDebugTileProviders = true
                isDebugMapTileDownloader = true
            }

            osmdroidBasePath = OsmConfiguration.getOsmDroidBaseDirectory(applicationContext)
            osmdroidTileCache = OsmConfiguration.getOsmDroidCacheDirectory(applicationContext)

            load(
                applicationContext,
                applicationContext.getSharedPreferences(
                    KEY_GLOBAL_SHARED_PREFERENCES,
                    Context.MODE_PRIVATE
                )
            )
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}