package hu.mostoha.mobile.android.turistautak.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.constants.HUNGARY_BOUNDING_BOX
import hu.mostoha.mobile.android.turistautak.constants.MY_LOCATION_MIN_DISTANCE_METER
import hu.mostoha.mobile.android.turistautak.constants.MY_LOCATION_MIN_TIME_MS
import hu.mostoha.mobile.android.turistautak.domain.model.toMapBoundingBox
import hu.mostoha.mobile.android.turistautak.extensions.*
import hu.mostoha.mobile.android.turistautak.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchBarAdapter
import kotlinx.android.synthetic.main.activity_home.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.io.File


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val TILES_SCALE_FACTOR = 1.25f
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
    }

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var layerDownloadReceiver: BroadcastReceiver

    private lateinit var searchBarAdapter: SearchBarAdapter

    private var myLocationOverlay: MyLocationOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWindow()
        initViews()
        initObservers()
        initReceivers()

        homeMapView.post {
            homeMapView.zoomToBoundingBox(HUNGARY_BOUNDING_BOX.toMapBoundingBox(), false)
        }
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)
        setFullScreenAndLightSystemBars()
        searchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun showMyLocation() {
        homeMyLocationButton.imageTintList = colorStateList(R.color.colorPrimary)
        if (myLocationOverlay == null) {
            val provider = GpsMyLocationProvider(applicationContext).apply {
                locationUpdateMinTime = MY_LOCATION_MIN_TIME_MS
                locationUpdateMinDistance = MY_LOCATION_MIN_DISTANCE_METER
            }
            myLocationOverlay = MyLocationOverlay(provider, homeMapView).apply {
                setDirectionArrow(
                    R.drawable.ic_marker_my_location.toBitmap(this@HomeActivity),
                    R.drawable.ic_marker_my_location_compass.toBitmap(this@HomeActivity)
                )
                onFollowLocationDisabled = {
                    homeMyLocationButton.imageTintList = colorStateList(R.color.colorPrimaryIcon)
                }
            }
        }
        myLocationOverlay?.apply {
            enableMyLocation()
            enableFollowLocation()
            enableAutoStop = true
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
        homeMapView.apply {
            tilesScaleFactor = TILES_SCALE_FACTOR
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
        }

        homeMyLocationButton.setOnClickListener {
            checkLocationPermissions(
                onPermissionsChecked = {
                    showMyLocation()
                },
                onPermissionRationaleShouldBeShown = {
                    MaterialAlertDialogBuilder(this@HomeActivity, R.style.DefaultMaterialDialog)
                        .setTitle(R.string.my_location_rationale_title)
                        .setMessage(R.string.my_location_rationale_message)
                        .setNegativeButton(R.string.my_location_rationale_negative_button) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(R.string.my_location_rationale_positive_button) { _, _ ->
                            it.continuePermissionRequest()
                        }
                        .show()
                })
        }

        homeSearchBarInputLayout.setEndIconOnClickListener {
            homeSearchBarInput.text?.clear()
            homeSearchBarInput.clearFocusAndHideKeyboard()
            // TODO: cancel current search job and hide loading
        }
        searchBarAdapter = SearchBarAdapter(this)
        homeSearchBarInput.setDropDownBackgroundResource(R.color.home_autocomplete_dropdown_color)
        homeSearchBarInput.setAdapter(searchBarAdapter)
        homeSearchBarInput.addTextChangedListener {
            if (!homeSearchBarInput.isPerformingCompletion) {
                val text = it.toString()
                if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                    viewModel.searchHikingRelationsBy(text)
                }
            } else {
                homeSearchBarInput.text?.clear()
                homeSearchBarInput.clearFocusAndHideKeyboard()
                // TODO: show result in map
            }
        }
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
                is SearchBarLoading -> {
                    homeSearchBarProgress.visibleOrInvisible(it.inProgress)
                }
                is SearchResult -> {
                    searchBarAdapter.clear()
                    searchBarAdapter.addAll(it.results)
                    homeSearchBarInput.refreshAutoCompleteResults()
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

        myLocationOverlay?.enableMyLocation()
        homeMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        myLocationOverlay?.disableMyLocation()
        myLocationOverlay?.disableFollowLocation()
        homeMapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        unregisterReceiver(layerDownloadReceiver)
    }

}



