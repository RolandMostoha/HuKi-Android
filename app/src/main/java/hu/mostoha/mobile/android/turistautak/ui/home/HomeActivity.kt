package hu.mostoha.mobile.android.turistautak.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ListPopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.*
import hu.mostoha.mobile.android.turistautak.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.turistautak.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.turistautak.model.ui.UiPayLoad
import hu.mostoha.mobile.android.turistautak.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.turistautak.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.turistautak.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.turistautak.ui.home.searchbar.SearchBarAdapter
import hu.mostoha.mobile.android.turistautak.util.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.item_home_landscapes_chip.view.*
import kotlinx.android.synthetic.main.layout_bottom_sheet_hiking_routes.*
import kotlinx.android.synthetic.main.layout_bottom_sheet_place_details.*
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import java.io.File


@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
        private const val OVERLAY_POSITION_HIKING_LAYER = 0
        private const val OVERLAY_POSITION_MY_LOCATION = 1
    }

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var layerDownloadReceiver: BroadcastReceiver

    private lateinit var searchBarPopup: ListPopupWindow
    private lateinit var searchBarAdapter: SearchBarAdapter
    private lateinit var placeDetailsSheet: BottomSheetBehavior<View>
    private lateinit var hikingRoutesSheet: BottomSheetBehavior<View>

    private var myLocationOverlay: MyLocationOverlay? = null

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
        homeSearchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun initViews() {
        homeMapView.apply {
            tilesScaleFactor = MAP_TILES_SCALE_FACTOR
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            addOnFirstLayoutListener { _, _, _, _, _ ->
                viewModel.loadHikingLayer()

                homeMapView.zoomToBoundingBox(HUNGARY.toOsmBoundingBox().withDefaultOffset(), false)

                if (isLocationPermissionsGranted()) {
                    showMyLocation()
                }
            }
            addZoomListener {
                if (it.zoomLevel >= MAP_ZOOM_THRESHOLD_ROUTES_NEARBY) {
                    homeRoutesNearbyFab.show()
                } else {
                    homeRoutesNearbyFab.hide()
                }
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
        searchBarPopup = ListPopupWindow(this).apply {
            anchorView = homeSearchBarInput
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.background_home_search_bar_dropdown
                )
            )
            setAdapter(searchBarAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val place = searchBarAdapter.getItem(position)
                if (place != null) {
                    myLocationOverlay?.disableFollowLocation()

                    homeSearchBarInput.text?.clear()
                    homeSearchBarInput.clearFocusAndHideKeyboard()
                    searchBarPopup.dismiss()

                    viewModel.loadPlaceDetails(place)
                }
            }
        }
        homeSearchBarInput.apply {
            addTextChangedListener {
                val text = it.toString()
                if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                    viewModel.loadPlacesBy(text)
                }
            }
        }

        homeLayerFab.setOnClickListener {
            // TODO
        }

        homeRoutesNearbyFab.setOnClickListener {
            viewModel.loadHikingRoutes(
                getString(R.string.map_place_name_routes_nearby),
                homeMapView.boundingBox.toDomainBoundingBox()
            )
        }

        placeDetailsSheet = BottomSheetBehavior.from(placeDetailsContainer)
        placeDetailsSheet.hide()

        hikingRoutesSheet = BottomSheetBehavior.from(hikingRoutesContainer)
        hikingRoutesSheet.hide()
    }

    private fun showMyLocation() {
        if (myLocationOverlay == null) {
            homeMyLocationButton.setImageResource(R.drawable.ic_anim_my_location_not_fixed)
            homeMyLocationButton.startDrawableAnimation()

            val provider = GpsMyLocationProvider(applicationContext).apply {
                locationUpdateMinTime = MY_LOCATION_MIN_TIME_MS
                locationUpdateMinDistance = MY_LOCATION_MIN_DISTANCE_METER
            }
            myLocationOverlay = MyLocationOverlay(provider, homeMapView).apply {
                setDirectionArrow(
                    R.drawable.ic_marker_my_location.toBitmap(this@HomeActivity),
                    R.drawable.ic_marker_my_location_compass.toBitmap(this@HomeActivity)
                )
                runOnFirstFix {
                    homeMyLocationButton.setImageResource(R.drawable.ic_action_my_location_fixed)
                }
                onFollowLocationDisabled = {
                    homeMyLocationButton.setImageResource(R.drawable.ic_action_my_location_not_fixed)
                }
                homeMapView.addOverlay(OVERLAY_POSITION_MY_LOCATION, this)
            }
        } else {
            homeMyLocationButton.setImageResource(R.drawable.ic_action_my_location_fixed)
        }

        myLocationOverlay?.apply {
            enableMyLocation()
            enableFollowLocation()
            enableAutoStop = true
        }
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
        homeMapView.addOverlay(OVERLAY_POSITION_HIKING_LAYER, overlay)
    }

    private fun initObservers() {
        viewModel.liveEvents.observe(this, {
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
                    searchBarPopup.width = homeSearchBarInputLayout.width
                    searchBarPopup.show()
                }
                is LandscapesResult -> {
                    it.landscapes.forEach { landscape ->
                        with(inflateLayout(R.layout.item_home_landscapes_chip, homeLandscapeChipGroup)) {
                            landscapesChip.text = landscape.primaryText
                            landscapesChip.setChipIconResource(landscape.iconRes)
                            landscapesChip.setOnClickListener {
                                myLocationOverlay?.disableFollowLocation()
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
                                    placeDetailsSheet.collapse()

                                    homeMapView.controller.animateTo(geoPoint)
                                }
                            )
                            initNodeBottomSheet(it.placeDetails.place, geoPoint, marker)
                            placeDetailsSheet.collapse()

                            homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
                        }
                        is UiPayLoad.Way -> {
                            val geoPoints = it.placeDetails.payLoad.geoPoints
                            val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                            val polyOverlay = if (it.placeDetails.payLoad.isClosed) {
                                homeMapView.addPolygon(geoPoints, onClick = { polygon ->
                                    initWayBottomSheet(it.placeDetails.place, boundingBox, polygon)
                                    placeDetailsSheet.collapse()
                                    homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                                })
                            } else {
                                homeMapView.addPolyline(geoPoints, onClick = { polyline ->
                                    initWayBottomSheet(it.placeDetails.place, boundingBox, polyline)
                                    placeDetailsSheet.collapse()
                                    homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                                })
                            }

                            initWayBottomSheet(it.placeDetails.place, boundingBox, polyOverlay)
                            placeDetailsSheet.collapse()

                            homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                        }
                    }
                }
                is HikingRoutesResult -> {
                    val hikingRoutesAdapter = HikingRoutesAdapter(
                        onItemClick = { hikingRoute ->
                            hikingRoutesSheet.hide()
                            myLocationOverlay?.disableFollowLocation()
                            viewModel.loadHikingRouteDetails(hikingRoute)
                            homeMapView.removeOverlays()
                        },
                        onCloseClick = {
                            hikingRoutesSheet.hide()
                        }
                    )
                    hikingRoutesList.setHasFixedSize(true)
                    hikingRoutesList.adapter = hikingRoutesAdapter
                    hikingRoutesAdapter.submitList(it.hikingRoutes)
                    hikingRoutesEmptyView.visibleOrGone(it.hikingRoutes.size <= 1)

                    hikingRoutesSheet.collapse()
                }
                is HikingRouteDetailsResult -> {
                    val placeDetails = it.placeDetails
                    val payLoad = placeDetails.payLoad as UiPayLoad.Relation
                    val boundingBox = BoundingBox
                        .fromGeoPoints(payLoad.ways.flatMap { way -> way.geoPoints })
                        .withDefaultOffset()
                    val overlays = mutableListOf<Overlay>()
                    payLoad.ways.forEach { way ->
                        val geoPoints = way.geoPoints
                        val overlay = homeMapView.addPolyline(geoPoints, onClick = {
                            initWayBottomSheet(placeDetails.place, boundingBox, *overlays.toTypedArray())
                            placeDetailsSheet.collapse()
                            homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                        })

                        overlays.add(overlay)
                    }

                    initWayBottomSheet(placeDetails.place, boundingBox, *overlays.toTypedArray())
                    placeDetailsSheet.collapse()

                    homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                }
            }
        })
        viewModel.viewState.observe(this, {
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
        placeDetailsDirectionsButton.visible()
        placeDetailsDirectionsButton.setOnClickListener {
            startGoogleDirections(geoPoint)
        }
        placeDetailsHikingTrailsButton.setOnClickListener {
            placeDetailsSheet.hide()
            viewModel.loadHikingRoutes(
                getString(R.string.map_place_name_node_routes_nearby, place.primaryText),
                homeMapView.boundingBox.toDomainBoundingBox()
            )
        }
        placeDetailsCloseButton.setOnClickListener {
            homeMapView.removeMarker(marker)
            placeDetailsSheet.hide()
        }
    }

    private fun initWayBottomSheet(place: PlaceUiModel, boundingBox: BoundingBox, vararg overlays: Overlay) {
        fillPlaceInfo(place)
        placeDetailsDirectionsButton.gone()
        placeDetailsHikingTrailsButton.setOnClickListener {
            placeDetailsSheet.hide()
            viewModel.loadHikingRoutes(place.primaryText, boundingBox.toDomainBoundingBox())
        }
        placeDetailsCloseButton.setOnClickListener {
            placeDetailsSheet.hide()
            homeMapView.removeOverlay(*overlays)
        }
    }

    private fun fillPlaceInfo(placeUiModel: PlaceUiModel) {
        with(placeUiModel) {
            placeDetailsPrimaryText.text = primaryText
            placeDetailsSecondaryText.setTextOrGone(secondaryText)
            placeDetailsImage.setImageResource(iconRes)
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

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(layerDownloadReceiver)
    }

    private fun BoundingBox.withDefaultOffset(): BoundingBox {
        return withOffset(
            homeMapView,
            R.dimen.home_map_view_top_offset,
            R.dimen.home_map_view_bottom_offset,
            R.dimen.home_map_view_start_offset,
            R.dimen.home_map_view_end_offset
        )
    }

}

