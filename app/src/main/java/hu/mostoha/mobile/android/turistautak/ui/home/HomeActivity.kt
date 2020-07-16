package hu.mostoha.mobile.android.turistautak.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.*
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.ErrorOccurred
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.LayerLoading
import kotlinx.android.synthetic.main.activity_home.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var layerDownloadReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWindow()
        initMap()
        initViews()
        initObservers()
        initReceivers()
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)
        setFullScreenAndLightSystemBars()
        searchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun initMap() {
        homeMapView.apply {
            tilesScaleFactor = 1.5f
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
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

    private fun initOfflineLayer(file: File) {
        val offlineProvider = OfflineTileProvider(
            SimpleRegisterReceiver(this),
            arrayOf(file)
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
        mapController.setZoom(11.0)
        mapController.setCenter(GeoPoint(47.4979, 19.0402))
    }

    private fun initObservers() {
        viewModel.liveEvents.observe(this, Observer {
            when (it) {
                is ErrorOccurred -> {
                    showToast(it.messageRes)
                }
                is LayerLoading -> {
                    homeLayerFab.inProgress = it.inProgress
                }
            }
        })
        viewModel.viewState.observe(this, Observer {
            val file = it.hikingLayerFile
            if (file != null) {
                initOfflineLayer(file)
            } else {
                MaterialAlertDialogBuilder(this, R.style.DefaultMaterialDialog)
                    .setTitle(R.string.download_layer_dialog_title)
                    .setMessage(R.string.download_layer_dialog_message)
                    .setPositiveButton(R.string.download_layer_dialog_positive_button) { _, _ ->
                        viewModel.downloadHikingLayer()
                    }
                    .setNegativeButton(R.string.download_layer_dialog_negative_button) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        })
    }

    private fun initReceivers() {
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        layerDownloadReceiver = registerReceiver(intentFilter) { intent ->
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            viewModel.handleFileDownloaded(downloadId)
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.checkHikingLayer()

        homeMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        homeMapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(layerDownloadReceiver)
    }

}



