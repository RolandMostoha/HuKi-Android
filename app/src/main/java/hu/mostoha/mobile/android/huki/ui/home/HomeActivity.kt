package hu.mostoha.mobile.android.huki.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ListPopupWindow
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ActivityHomeBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeLandscapesChipBinding
import hu.mostoha.mobile.android.huki.databinding.WindowPopupLayersBinding
import hu.mostoha.mobile.android.huki.extensions.*
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.UiPayLoad
import hu.mostoha.mobile.android.huki.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.huki.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarAdapter
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.util.*
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
    }

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var layerDownloadReceiver: BroadcastReceiver

    private lateinit var binding: ActivityHomeBinding
    private lateinit var layersPopupBinding: WindowPopupLayersBinding

    private val homeMapView by lazy { binding.homeMapView }
    private val homeSearchBarContainer by lazy { binding.homeSearchBarContainer }
    private val homeSearchBarInputLayout by lazy { binding.homeSearchBarInputLayout }
    private val homeSearchBarInput by lazy { binding.homeSearchBarInput }
    private val homeMyLocationButton by lazy { binding.homeMyLocationButton }
    private val homeRoutesNearbyFab by lazy { binding.homeRoutesNearbyFab }
    private val homeLayersFab by lazy { binding.homeLayersFab }

    private lateinit var searchBarPopup: ListPopupWindow
    private lateinit var searchBarAdapter: SearchBarAdapter
    private lateinit var layersPopupWindow: PopupWindow
    private lateinit var placeDetailsSheet: BottomSheetBehavior<View>
    private lateinit var hikingRoutesSheet: BottomSheetBehavior<View>

    private var myLocationOverlay: MyLocationOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        initMapView()
        initSearchBar()
        initLayersPopupWindow()
        initFabs()
        initBottomSheets()
    }

    private fun initMapView() {
        homeMapView.apply {
            tilesScaleFactor = MAP_TILES_SCALE_FACTOR
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            addOnFirstLayoutListener { _, _, _, _, _ ->
                viewModel.loadHikingLayer()

                zoomToBoundingBox(HUNGARY.toOsmBoundingBox().withDefaultOffset(), false)

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
    }

    private fun initSearchBar() {
        searchBarAdapter = SearchBarAdapter(this)
        homeSearchBarInputLayout.setEndIconOnClickListener {
            homeSearchBarInput.text?.clear()
            homeSearchBarInput.clearFocusAndHideKeyboard()
            viewModel.cancelSearch()
        }
        homeSearchBarInput.addTextChangedListener {
            val text = it.toString()
            if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                viewModel.loadPlacesBy(text)
            }
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
                if (place != null && place is SearchBarItem.Place) {
                    myLocationOverlay?.disableFollowLocation()

                    homeSearchBarInput.text?.clear()
                    homeSearchBarInput.clearFocusAndHideKeyboard()
                    searchBarPopup.dismiss()

                    viewModel.loadPlaceDetails(place.placeUiModel)
                }
            }
        }
    }

    private fun initLayersPopupWindow() {
        layersPopupWindow = PopupWindow(this).apply {
            layersPopupBinding = WindowPopupLayersBinding.inflate(layoutInflater, null, false)
            contentView = layersPopupBinding.root
            setBackgroundDrawable(
                InsetDrawable(
                    ContextCompat.getDrawable(
                        this@HomeActivity,
                        R.drawable.background_home_search_bar_dropdown
                    ), 0, 0,
                    resources.getDimensionPixelSize(R.dimen.space_medium),
                    resources.getDimensionPixelSize(R.dimen.space_medium)
                )
            )
            width = WRAP_CONTENT
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
            isOutsideTouchable = true
            isFocusable = true
            elevation = resources.getDimension(R.dimen.default_elevation)
        }
    }

    private fun initFabs() {
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

        homeRoutesNearbyFab.setOnClickListener {
            viewModel.loadHikingRoutes(
                getString(R.string.map_place_name_routes_nearby),
                homeMapView.boundingBox.toDomainBoundingBox()
            )
        }

        homeLayersFab.setOnClickListener {
            showLayersDialog()
        }
    }

    private fun initBottomSheets() {
        placeDetailsSheet = BottomSheetBehavior.from(binding.homePlaceDetailsBottomSheetContainer.root)
        placeDetailsSheet.hide()

        hikingRoutesSheet = BottomSheetBehavior.from(binding.homeHikingRoutesBottomSheetContainer.root)
        hikingRoutesSheet.hide()
    }

    private fun showLayersDialog() {
        layersPopupWindow.showAsDropDown(homeLayersFab, 0, resources.getDimensionPixelSize(R.dimen.space_small))
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
                homeMapView.addOverlay(OverlayPositions.MY_LOCATION, this)
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
        with(homeMapView) {
            val offlineProvider = OfflineTileProvider(
                SimpleRegisterReceiver(this@HomeActivity),
                arrayOf(file)
            ).apply {
                // Issue: https://github.com/osmdroid/osmdroid/issues/690
                tileRequestCompleteHandlers.clear()
                tileRequestCompleteHandlers.add(tileRequestCompleteHandler)
            }
            val overlay = TilesOverlay(offlineProvider, applicationContext).apply {
                loadingBackgroundColor = Color.TRANSPARENT
                loadingLineColor = Color.TRANSPARENT
            }
            addOverlay(OverlayPositions.HIKING_LAYER, overlay)
        }
    }

    private fun initObservers() {
        viewModel.liveEvents.observe(this, { event ->
            when (event) {
                is ErrorOccurred -> {
                    showToast(event.message)
                }
                is LayerLoading -> {
                    homeLayersFab.inProgress = event.inProgress
                }
                is SearchBarLoading -> {
                    binding.homeSearchBarProgress.visibleOrGone(event.inProgress)
                }
                is PlacesResult -> {
                    initPlaceResult(event)
                }
                is PlacesErrorResult -> {
                    initPlaceErrorResult(event)
                }
                is LandscapesResult -> {
                    initLandscapeResult(event)
                }
                is PlaceDetailsResult -> {
                    initPlaceDetailsResult(event)
                }
                is HikingRoutesResult -> {
                    initHikingRoutesResult(event)
                }
                is HikingRouteDetailsResult -> {
                    initHikingRouteDetailsResult(event)
                }
            }
        })
        viewModel.viewState.observe(this, {
            val hikingLayerDetails = it.hikingLayerDetails

            updateLayersDialog(hikingLayerDetails)

            if (hikingLayerDetails.isHikingLayerFileDownloaded) {
                initOfflineLayer(hikingLayerDetails.hikingLayerFile!!)
            } else {
                showLayersDialog()
            }
        })
    }

    private fun initPlaceResult(placesResult: PlacesResult) {
        searchBarAdapter.submitList(placesResult.results.map { placeUiModel -> SearchBarItem.Place(placeUiModel) })
        searchBarPopup.apply {
            width = binding.homeSearchBarInputLayout.width
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
            show()
        }
    }

    private fun initPlaceErrorResult(placesErrorResult: PlacesErrorResult) {
        searchBarAdapter.submitList(
            listOf(SearchBarItem.Info(placesErrorResult.messageRes, placesErrorResult.drawableRes))
        )
        searchBarPopup.apply {
            width = binding.homeSearchBarInputLayout.width
            height = ListPopupWindow.WRAP_CONTENT
            show()
        }
    }

    private fun initLandscapeResult(landscapesResult: LandscapesResult) {
        landscapesResult.landscapes.forEach { landscape ->
            val chipBinding = ItemHomeLandscapesChipBinding.inflate(
                layoutInflater,
                binding.homeLandscapeChipGroup,
                false
            )
            chipBinding.bindUiModel(
                uiModel = landscape,
                onChipClick = {
                    myLocationOverlay?.disableFollowLocation()
                    viewModel.loadPlaceDetails(landscape)
                }
            )
            binding.homeLandscapeChipGroup.addView(chipBinding.root)
        }
    }

    private fun initPlaceDetailsResult(placeDetailsResult: PlaceDetailsResult) {
        val placeDetails = placeDetailsResult.placeDetails
        when (placeDetails.payLoad) {
            is UiPayLoad.Node -> {
                val geoPoint = placeDetails.payLoad.geoPoint
                val marker = homeMapView.addMarker(geoPoint, R.drawable.ic_marker_poi.toDrawable(this),
                    onClick = { marker ->
                        initNodeBottomSheet(placeDetails.place, geoPoint, marker)
                        placeDetailsSheet.collapse()

                        homeMapView.controller.animateTo(geoPoint)
                    }
                )
                initNodeBottomSheet(placeDetails.place, geoPoint, marker)
                placeDetailsSheet.collapse()

                homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
            }
            is UiPayLoad.Way -> {
                val geoPoints = placeDetails.payLoad.geoPoints
                val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
                val polyOverlay = if (placeDetails.payLoad.isClosed) {
                    homeMapView.addPolygon(geoPoints, onClick = { polygon ->
                        initWayBottomSheet(placeDetails.place, boundingBox, polygon)
                        placeDetailsSheet.collapse()
                        homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                    })
                } else {
                    homeMapView.addPolyline(geoPoints, onClick = { polyline ->
                        initWayBottomSheet(placeDetails.place, boundingBox, polyline)
                        placeDetailsSheet.collapse()
                        homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                    })
                }

                initWayBottomSheet(placeDetails.place, boundingBox, polyOverlay)
                placeDetailsSheet.collapse()

                homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
            }
            else -> {
                // no-op
            }
        }
    }

    private fun initHikingRoutesResult(hikingRoutesResult: HikingRoutesResult) {
        with(binding.homeHikingRoutesBottomSheetContainer) {
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
            hikingRoutesAdapter.submitList(hikingRoutesResult.hikingRoutes)
            hikingRoutesEmptyView.visibleOrGone(hikingRoutesResult.hikingRoutes.size <= 1)

            hikingRoutesSheet.collapse()
        }
    }

    private fun initHikingRouteDetailsResult(it: HikingRouteDetailsResult) {
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

    private fun initNodeBottomSheet(placeUiModel: PlaceUiModel, geoPoint: GeoPoint, marker: Marker) {
        binding.homePlaceDetailsBottomSheetContainer.bindNodeUiModel(
            place = placeUiModel,
            onHikingTrailsButtonClick = {
                placeDetailsSheet.hide()
                viewModel.loadHikingRoutes(
                    getString(R.string.map_place_name_node_routes_nearby, placeUiModel.primaryText),
                    homeMapView.boundingBox.toDomainBoundingBox()
                )
            },
            onCloseButtonClick = {
                homeMapView.removeMarker(marker)
                placeDetailsSheet.hide()
            },
            onDirectionsButtonClick = {
                startGoogleDirections(geoPoint)
            }
        )
    }

    private fun initWayBottomSheet(placeUiModel: PlaceUiModel, boundingBox: BoundingBox, vararg overlays: Overlay) {
        binding.homePlaceDetailsBottomSheetContainer.bindWayUiModel(
            uiModel = placeUiModel,
            onHikingTrailsButtonClick = {
                placeDetailsSheet.hide()
                viewModel.loadHikingRoutes(placeUiModel.primaryText, boundingBox.toDomainBoundingBox())
            },
            onCloseButtonClick = {
                placeDetailsSheet.hide()
                homeMapView.removeOverlay(*overlays)
            }
        )
    }

    private fun updateLayersDialog(hikingLayerDetails: HikingLayerDetailsUiModel) {
        layersPopupBinding.bindUiModel(
            uiModel = hikingLayerDetails,
            onDownloadButtonClick = { viewModel.downloadHikingLayer() }
        )
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
