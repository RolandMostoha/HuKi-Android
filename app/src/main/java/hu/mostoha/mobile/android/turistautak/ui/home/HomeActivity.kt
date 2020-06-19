package hu.mostoha.mobile.android.turistautak.ui.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hu.mostoha.mobile.android.turistautak.BuildConfig
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.configuration.OsmConfiguration
import hu.mostoha.mobile.android.turistautak.extensions.*
import kotlinx.android.synthetic.main.activity_home.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        initWindow()
        initMap()
        initViews()
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)
        setFullScreenAndLightSystemBars()
        searchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun initMap() {
        // TODO: move to application level
        Configuration.getInstance().apply {
            if (BuildConfig.DEBUG) {
                isDebugMapView = true
                isDebugMode = true
                isDebugTileProviders = true
                isDebugMapTileDownloader = true
            }

            osmdroidBasePath = OsmConfiguration.getOsmDroidBasePath(applicationContext)
            osmdroidTileCache = OsmConfiguration.getOsmDroidCachePath(applicationContext)

            load(applicationContext, getPreferences(Context.MODE_PRIVATE))
        }

        homeMapView.apply {
            tilesScaleFactor = 1.5f
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
        }

        if (OsmConfiguration.isHikingLayerFileExist(applicationContext)) {
            initOfflineLayer()
        }

        initMyLocation()
    }

    private fun initMyLocation() {
        val provider = GpsMyLocationProvider(applicationContext)
        val myLocationOverlay = MyLocationNewOverlay(provider, homeMapView)
        myLocationOverlay.apply {
            setPersonIcon(R.drawable.ic_marker_my_location.toBitmap(this@HomeActivity))
            setDirectionArrow(
                R.drawable.ic_marker_my_location.toBitmap(this@HomeActivity),
                R.drawable.ic_marker_my_location.toBitmap(this@HomeActivity)
            )
            enableMyLocation()
        }
        homeMapView.overlays.add(myLocationOverlay)
        homeMapView.invalidate()
    }

    private fun initOfflineLayer() {
        val offlineProvider = OfflineTileProvider(
            SimpleRegisterReceiver(this),
            arrayOf(OsmConfiguration.getHikingLayerFile(applicationContext))
        )

        val overlay = TilesOverlay(offlineProvider, this)
        overlay.loadingBackgroundColor = Color.TRANSPARENT
        overlay.loadingLineColor = Color.TRANSPARENT

        homeMapView.overlays.add(overlay)
        homeMapView.invalidate()
    }

    private fun initViews() {
        homeSearchBarInputLayout.setEndIconOnClickListener {
            homeSearchBarInput.text?.clear()
            homeSearchBarInput.clearFocusAndHideKeyboard()
        }

        val mapController = homeMapView.controller
        mapController.setZoom(10.0)
        mapController.setCenter(GeoPoint(47.4979, 19.0402))
    }

    override fun onResume() {
        super.onResume()

        homeMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        homeMapView.onPause()
    }

}



