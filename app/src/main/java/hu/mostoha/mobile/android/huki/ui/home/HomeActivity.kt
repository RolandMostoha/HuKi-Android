package hu.mostoha.mobile.android.huki.ui.home

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
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ActivityHomeBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeLandscapesChipBinding
import hu.mostoha.mobile.android.huki.extensions.*
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikingLayerDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.huki.ui.home.HomeLiveEvents.*
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersPopupWindow
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

    private val homeContainer by lazy { binding.homeContainer }
    private val homeMapView by lazy { binding.homeMapView }
    private val homeSearchBarContainer by lazy { binding.homeSearchBarContainer }
    private val homeSearchBarInputLayout by lazy { binding.homeSearchBarInputLayout }
    private val homeSearchBarInput by lazy { binding.homeSearchBarInput }
    private val homeMyLocationButton by lazy { binding.homeMyLocationButton }
    private val homeRoutesNearbyFab by lazy { binding.homeRoutesNearbyFab }
    private val homeLayersFab by lazy { binding.homeLayersFab }

    private lateinit var searchBarPopup: ListPopupWindow
    private lateinit var searchBarAdapter: SearchBarAdapter
    private lateinit var layersPopupWindow: LayersPopupWindow
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

                zoomToBoundingBox(HUNGARY_BOUNDING_BOX.toOsmBoundingBox().withDefaultOffset(), false)

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
            height = resources.getDimensionPixelSize(R.dimen.home_layers_popup_height)
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.drawable.background_dialog
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

                    viewModel.loadPlace(place.placeUiModel)
                }
            }
        }
    }

    private fun initLayersPopupWindow() {
        layersPopupWindow = LayersPopupWindow(this)
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
                is ErrorOccurred -> showSnackbar(homeContainer, event.message)
                is LayerLoading -> homeLayersFab.inProgress = event.inProgress
                is SearchBarLoading -> binding.homeSearchBarProgress.visibleOrGone(event.inProgress)
                is PlacesResult -> initPlaces(event.places)
                is PlacesErrorResult -> initPlacesErrorResult(event)
                is PlaceResult -> initPlace(event.place)
                is LandscapesResult -> initLandscapeResult(event.landscapes)
                is PlaceDetailsResult -> initPlaceDetails(event.placeDetails)
                is HikingRoutesResult -> initHikingRoutesResult(event.hikingRoutes)
                is HikingRouteDetailsResult -> initHikingRouteDetailsResult(event.placeDetails)
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

    private fun initPlaces(places: List<PlaceUiModel>) {
        searchBarAdapter.submitList(places.map { placeUiModel -> SearchBarItem.Place(placeUiModel) })
        searchBarPopup.apply {
            width = binding.homeSearchBarInputLayout.width
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
            show()
        }
    }

    private fun initPlacesErrorResult(placesErrorResult: PlacesErrorResult) {
        searchBarAdapter.submitList(
            listOf(SearchBarItem.Info(placesErrorResult.messageRes, placesErrorResult.drawableRes))
        )
        searchBarPopup.apply {
            width = binding.homeSearchBarInputLayout.width
            height = ListPopupWindow.WRAP_CONTENT
            show()
        }
    }

    private fun initLandscapeResult(landscapes: List<PlaceUiModel>) {
        landscapes.forEach { landscape ->
            val chipBinding = ItemHomeLandscapesChipBinding.inflate(
                layoutInflater,
                binding.homeLandscapeChipGroup,
                false
            )
            chipBinding.landscapesChip.text = landscape.primaryText
            chipBinding.landscapesChip.setChipIconResource(landscape.iconRes)
            chipBinding.landscapesChip.setOnClickListener {
                myLocationOverlay?.disableFollowLocation()
                viewModel.loadPlaceDetails(landscape)
            }
            binding.homeLandscapeChipGroup.addView(chipBinding.root)
        }
    }

    private fun initPlace(placeUiModel: PlaceUiModel) {
        val geoPoint = placeUiModel.geoPoint
        val boundingBox = placeUiModel.boundingBox

        val marker = homeMapView.addMarker(
            geoPoint = geoPoint,
            icon = R.drawable.ic_marker_poi.toDrawable(this),
            onClick = { marker ->
                initNodeBottomSheet(placeUiModel, geoPoint, marker)
                placeDetailsSheet.collapse()

                if (boundingBox != null) {
                    homeMapView.zoomToBoundingBox(boundingBox.toOsmBoundingBox().withDefaultOffset(), true)
                } else {
                    homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
                }
            }
        )
        initNodeBottomSheet(placeUiModel, geoPoint, marker)
        placeDetailsSheet.collapse()

        if (boundingBox != null) {
            homeMapView.zoomToBoundingBox(boundingBox.toOsmBoundingBox(), true)
        } else {
            homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
        }
    }

    private fun initPlaceDetails(placeDetails: PlaceDetailsUiModel) {
        when (placeDetails.geometryUiModel) {
            is GeometryUiModel.Way -> initWayDetails(placeDetails)
            is GeometryUiModel.Relation -> initRelationDetails(placeDetails)
            else -> {
                // No-op: place details is not an option for Nodes
            }
        }
    }

    private fun initWayDetails(placeDetails: PlaceDetailsUiModel) {
        val geometryUiModel = placeDetails.geometryUiModel as? GeometryUiModel.Way ?: return

        val geoPoints = geometryUiModel.geoPoints
        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
        val polyOverlay = if (geometryUiModel.isClosed) {
            homeMapView.addPolygon(
                geoPoints = geoPoints,
                onClick = { polygon ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polygon))
                    placeDetailsSheet.collapse()
                    homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                }
            )
        } else {
            homeMapView.addPolyline(
                geoPoints = geoPoints,
                onClick = { polyline ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyline))
                    placeDetailsSheet.collapse()
                    homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
                }
            )
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyOverlay))
        placeDetailsSheet.collapse()

        homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
    }

    private fun initRelationDetails(placeDetails: PlaceDetailsUiModel) {
        val relation = placeDetails.geometryUiModel as? GeometryUiModel.Relation ?: return
        val boundingBox = BoundingBox
            .fromGeoPoints(relation.ways.flatMap { it.geoPoints })
            .withDefaultOffset()
        val overlays = mutableListOf<Overlay>()
        relation.ways.forEach { way ->
            val geoPoints = way.geoPoints
            val overlay = homeMapView.addPolyline(geoPoints, onClick = {
                initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)
                placeDetailsSheet.collapse()
                homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
            })

            overlays.add(overlay)
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)
        placeDetailsSheet.collapse()

        homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(), true)
    }

    private fun initHikingRoutesResult(hikingRoutes: List<HikingRoutesItem>) {
        with(binding.homeHikingRoutesBottomSheetContainer) {
            val hikingRoutesAdapter = HikingRoutesAdapter(
                onItemClick = { hikingRoute ->
                    hikingRoutesSheet.hide()
                    myLocationOverlay?.disableFollowLocation()
                    viewModel.loadHikingRouteDetails(hikingRoute)
                    homeMapView.removePlaceOverlays()
                },
                onCloseClick = {
                    hikingRoutesSheet.hide()
                }
            )
            hikingRoutesList.setHasFixedSize(true)
            hikingRoutesList.adapter = hikingRoutesAdapter
            hikingRoutesAdapter.submitList(hikingRoutes)
            hikingRoutesEmptyView.visibleOrGone(hikingRoutes.size <= 1)

            hikingRoutesSheet.collapse()
        }
    }

    private fun initHikingRouteDetailsResult(placeDetails: PlaceDetailsUiModel) {
        initRelationDetails(placeDetails)
    }

    private fun initNodeBottomSheet(placeUiModel: PlaceUiModel, geoPoint: GeoPoint, marker: Marker) {
        with(binding.homePlaceDetailsBottomSheetContainer) {
            placeDetailsPrimaryText.text = placeUiModel.primaryText
            placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
            placeDetailsImage.setImageResource(placeUiModel.iconRes)
            placeDetailsDirectionsButton.visible()
            placeDetailsDirectionsButton.setOnClickListener {
                startGoogleDirections(geoPoint)
            }
            if (placeUiModel.placeType != PlaceType.NODE) {
                placeDetailsHikingTrailsButton.gone()
                placeDetailsShowPointsButton.visible()
                placeDetailsShowPointsButton.setOnClickListener {
                    placeDetailsSheet.hide()
                    homeMapView.removePlaceOverlays()
                    viewModel.loadPlaceDetails(placeUiModel)
                }
            } else {
                placeDetailsShowPointsButton.gone()
                placeDetailsHikingTrailsButton.visible()
                placeDetailsHikingTrailsButton.setOnClickListener {
                    placeDetailsSheet.hide()
                    viewModel.loadHikingRoutes(
                        getString(R.string.map_place_name_node_routes_nearby, placeUiModel.primaryText),
                        homeMapView.boundingBox.toDomainBoundingBox()
                    )
                }
            }
            placeDetailsCloseButton.setOnClickListener {
                homeMapView.removeMarker(marker)
                placeDetailsSheet.hide()
            }
        }
    }

    private fun initWayBottomSheet(placeUiModel: PlaceUiModel, boundingBox: BoundingBox, overlays: List<Overlay>) {
        with(binding.homePlaceDetailsBottomSheetContainer) {
            placeDetailsPrimaryText.text = placeUiModel.primaryText
            placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
            placeDetailsImage.setImageResource(placeUiModel.iconRes)
            placeDetailsDirectionsButton.gone()
            placeDetailsShowPointsButton.gone()
            placeDetailsHikingTrailsButton.visible()
            placeDetailsHikingTrailsButton.setOnClickListener {
                placeDetailsSheet.hide()
                viewModel.loadHikingRoutes(placeUiModel.primaryText, boundingBox.toDomainBoundingBox())
            }
            placeDetailsCloseButton.setOnClickListener {
                placeDetailsSheet.hide()
                if (overlays.isNotEmpty()) {
                    homeMapView.removeOverlay(overlays)
                }
            }
        }
    }

    private fun updateLayersDialog(hikingLayerDetails: HikingLayerDetailsUiModel) {
        layersPopupWindow.updateDialog(
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
