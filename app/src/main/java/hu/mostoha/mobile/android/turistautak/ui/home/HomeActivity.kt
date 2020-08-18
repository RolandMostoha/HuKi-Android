package hu.mostoha.mobile.android.turistautak.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.constants.HUNGARY_BOUNDING_BOX
import hu.mostoha.mobile.android.turistautak.constants.MY_LOCATION_MIN_DISTANCE_METER
import hu.mostoha.mobile.android.turistautak.constants.MY_LOCATION_MIN_TIME_MS
import hu.mostoha.mobile.android.turistautak.extensions.*
import hu.mostoha.mobile.android.turistautak.model.domain.toMapBoundingBox
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.UiPayLoad
import hu.mostoha.mobile.android.turistautak.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchBarAdapter
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_home_landscapes_chip.view.*
import kotlinx.android.synthetic.main.layout_home_bottom_sheet.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.io.File


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val DEFAULT_ZOOM_LEVEL = 15
        private const val TILES_SCALE_FACTOR = 1.5f
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
    }

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var layerDownloadReceiver: BroadcastReceiver

    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private lateinit var searchBarAdapter: SearchBarAdapter

    private val bottomSheetExclusiveButtons by lazy {
        listOf(
            homeBottomSheetDirectionsButton,
            homeBottomSheetHikingTrailsButton
        )
    }

    private var myLocationOverlay: MyLocationOverlay? = null

    private val boundsOffsetHu by lazy { resources.getDimensionPixelSize(R.dimen.home_bounding_box_offset) }
    private val boundsOffsetRoutes by lazy { resources.getDimensionPixelSize(R.dimen.home_bounding_box_offset_routes) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWindow()
        initViews()
        initObservers()
        initReceivers()

        viewModel.loadLandscapes()
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)
        setFullScreenAndLightSystemBars()
        homeSearchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun initViews() {
        homeMapView.apply {
            tilesScaleFactor = TILES_SCALE_FACTOR
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            addOnFirstLayoutListener { _, _, _, _, _ ->
                viewModel.loadHikingLayer()

                homeMapView.zoomToBoundingBox(HUNGARY_BOUNDING_BOX.toMapBoundingBox(), false, boundsOffsetHu)
            }
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

        searchBarAdapter = SearchBarAdapter(this)
        homeSearchBarInputLayout.setEndIconOnClickListener {
            homeSearchBarInput.text?.clear()
            homeSearchBarInput.clearFocusAndHideKeyboard()
            viewModel.cancelSearch()
        }
        homeSearchBarInput.apply {
            setDropDownBackgroundResource(R.drawable.background_home_search_bar_dropdown)
            setAdapter(searchBarAdapter)
            threshold = SEARCH_BAR_MIN_TRIGGER_LENGTH
            addTextChangedListener {
                if (!homeSearchBarInput.isPerformingCompletion) {
                    val text = it.toString()
                    if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                        viewModel.loadPlacesBy(text)
                    }
                } else {
                    homeSearchBarInput.text?.clear()
                    homeSearchBarInput.clearFocusAndHideKeyboard()
                }
            }
            setOnItemClickListener { _, _, position, _ ->
                val place = searchBarAdapter.getItem(position)
                if (place != null) {
                    viewModel.loadPlaceDetails(place)
                }
            }
        }
        sheetBehavior = BottomSheetBehavior.from(homeBottomSheetContainer)
        sheetBehavior.hide()
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
        val offlineProvider = OfflineTileProvider(SimpleRegisterReceiver(this), arrayOf(file)).apply {
            // Issue: https://github.com/osmdroid/osmdroid/issues/690
            tileRequestCompleteHandlers.clear()
            tileRequestCompleteHandlers.add(homeMapView.tileRequestCompleteHandler)
        }
        val overlay = TilesOverlay(offlineProvider, this).apply {
            loadingBackgroundColor = Color.TRANSPARENT
            loadingLineColor = Color.TRANSPARENT
        }
        homeMapView.overlays.add(overlay)
        homeMapView.invalidate()
    }

    private fun initObservers() {
        viewModel.liveEvents.observe(this, Observer {
            when (it) {
                is ErrorOccurred -> {
                    showToast(it.message)
                }
                is LayerLoading -> {
                    homeLayerFab.inProgress = it.inProgress
                }
                is SearchBarLoading -> {
                    homeSearchBarProgress.visibleOrGone(it.inProgress)
                }
                is PlacesResult -> {
                    searchBarAdapter.submitList(it.results)
                    homeSearchBarInput.refreshAutoCompleteResults()
                }
                is LandscapesResult -> {
                    it.landscapes.forEach { landscape ->
                        with(inflateLayout(R.layout.item_home_landscapes_chip, homeLandscapeChipGroup)) {
                            landscapesChip.text = landscape.primaryText
                            landscapesChip.setChipIconResource(landscape.iconRes)
                            landscapesChip.setOnClickListener {
                                viewModel.loadPlaceDetails(landscape)
                            }
                            homeLandscapeChipGroup.addView(this)
                        }
                    }
                }
                is PlaceDetailsResult -> {
                    when (it.placeDetails.payLoad) {
                        is UiPayLoad.Node -> {
                            val geoPoint = it.placeDetails.payLoad.geoPoint
                            val marker = homeMapView.addMarker(geoPoint, R.drawable.ic_marker_poi.toDrawable(this),
                                onClick = { marker ->
                                    initNodeBottomSheet(it.placeDetails.place, geoPoint, marker)
                                    sheetBehavior.collapse()

                                    homeMapView.controller.animateTo(geoPoint)
                                }
                            )
                            initNodeBottomSheet(it.placeDetails.place, geoPoint, marker)
                            sheetBehavior.collapse()

                            homeMapView.animateCenterAndZoom(geoPoint, DEFAULT_ZOOM_LEVEL)
                        }
                        is UiPayLoad.Way -> {
                            val geoPoints = it.placeDetails.payLoad.geoPoints
                            val polygon = homeMapView.addPolygon(geoPoints, onClick = { polygon ->
                                initWayBottomSheet(it.placeDetails.place, polygon)
                                sheetBehavior.collapse()

                                val bounds = BoundingBox.fromGeoPoints(geoPoints)
                                homeMapView.zoomToBoundingBox(bounds, true, boundsOffsetRoutes)
                            })

                            initWayBottomSheet(it.placeDetails.place, polygon)
                            sheetBehavior.collapse()

                            val bounds = BoundingBox.fromGeoPoints(geoPoints)
                            homeMapView.zoomToBoundingBox(bounds, true, boundsOffsetRoutes)
                        }
                        is UiPayLoad.Relation -> {
                            val geoPoints = it.placeDetails.payLoad.geoPoints
                            homeMapView.addPolygon(geoPoints, onClick = {

                            })

                            val bounds = BoundingBox.fromGeoPoints(geoPoints)
                            homeMapView.zoomToBoundingBox(bounds, true, boundsOffsetRoutes)

                            bottomSheetExclusiveButtons.showOnly(homeBottomSheetHikingTrailsButton)
                            sheetBehavior.collapse()
                        }
                    }
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

    private fun initNodeBottomSheet(place: PlaceUiModel, geoPoint: GeoPoint, marker: Marker) {
        fillPlaceInfo(place)
        bottomSheetExclusiveButtons.showOnly(homeBottomSheetDirectionsButton)
        homeBottomSheetDirectionsButton.setOnClickListener {
            startDirectionsTo(geoPoint)
        }
        homeBottomSheetCloseButton.setOnClickListener {
            homeMapView.removeMarker(marker)
            sheetBehavior.hide()
        }
    }

    private fun initWayBottomSheet(place: PlaceUiModel, polygon: Polygon) {
        fillPlaceInfo(place)
        bottomSheetExclusiveButtons.showOnly(homeBottomSheetHikingTrailsButton)
        homeBottomSheetHikingTrailsButton.setOnClickListener {
            // TODO: Search for hiking trails
        }
        homeBottomSheetCloseButton.setOnClickListener {
            homeMapView.removePolygon(polygon)
            sheetBehavior.hide()
        }
    }

    private fun fillPlaceInfo(placeUiModel: PlaceUiModel) {
        with(placeUiModel) {
            homeBottomSheetPrimaryText.text = primaryText
            homeBottomSheetSecondaryText.setTextOrGone(secondaryText)
            homeBottomSheetImage.setImageResource(iconRes)
        }
    }

    private fun initReceivers() {
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        layerDownloadReceiver = registerReceiver(intentFilter) { intent ->
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            viewModel.loadDownloadedFile(downloadId)
        }
    }

    override fun onResume() {
        super.onResume()

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
