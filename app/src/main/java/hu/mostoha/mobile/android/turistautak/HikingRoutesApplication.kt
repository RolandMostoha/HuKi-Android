package hu.mostoha.mobile.android.turistautak

import android.app.Application
import android.content.Context
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import org.osmdroid.config.Configuration

class HikingRoutesApplication : Application() {

    companion object {
        const val KEY_GLOBAL_SHARED_PREFERENCES = "KEY_GLOBAL_SHARED_PREFERENCES"
    }

    override fun onCreate() {
        super.onCreate()

        initOsmDroid()
    }

    private fun initOsmDroid() {
        Configuration.getInstance().apply {
            if (BuildConfig.DEBUG) {
                isDebugMapView = true
                isDebugMode = true
                isDebugTileProviders = true
                isDebugMapTileDownloader = true
            }

            osmdroidBasePath = OsmConfiguration.getOsmDroidBasePath(applicationContext)
            osmdroidTileCache = OsmConfiguration.getOsmDroidCachePath(applicationContext)

            load(
                applicationContext,
                applicationContext.getSharedPreferences(
                    KEY_GLOBAL_SHARED_PREFERENCES,
                    Context.MODE_PRIVATE
                )
            )
        }
    }

}