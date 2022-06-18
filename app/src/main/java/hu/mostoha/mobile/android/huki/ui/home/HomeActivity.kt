package hu.mostoha.mobile.android.huki.ui.home

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ListPopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
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
import hu.mostoha.mobile.android.huki.model.ui.*
import hu.mostoha.mobile.android.huki.osmdroid.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.OsmAndOfflineTileProvider
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersPopupWindow
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarAdapter
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.MAP_TILES_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import hu.mostoha.mobile.android.huki.util.startGoogleDirections
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
    }

    @Inject
    lateinit var myLocationProvider: AsyncMyLocationProvider

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var layerDownloadReceiver: BroadcastReceiver
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val homeContainer by lazy { binding.homeContainer }
    private val homeMapView by lazy { binding.homeMapView }
    private val homeSearchBarContainer by lazy { binding.homeSearchBarContainer }
    private val homeSearchBarInputLayout by lazy { binding.homeSearchBarInputLayout }
    private val homeSearchBarInput by lazy { binding.homeSearchBarInput }
    private val homeSearchBarPopupAnchor by lazy { binding.homeSearchBarPopupAnchor }
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
        initPermissions()
        initReceivers()
    }

    override fun onResume() {
        super.onResume()

        if (isLocationPermissionGranted()) {
            enableMyLocationMonitoring()
        }

        homeMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        myLocationOverlay?.disableMyLocation()
        viewModel.saveBoundingBox(homeMapView.boundingBox.toDomainBoundingBox())

        homeMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(layerDownloadReceiver)
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
                initFlows()

                viewModel.loadHikingLayer()
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
        homeSearchBarInput.addTextChangedListener { editable ->
            val text = editable.toString()
            if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                viewModel.loadSearchBarPlaces(text)
            }
        }
        searchBarPopup = ListPopupWindow(this).apply {
            anchorView = homeSearchBarPopupAnchor
            height = resources.getDimensionPixelSize(R.dimen.home_layers_popup_height)
            setBackgroundDrawable(ContextCompat.getDrawable(this@HomeActivity, R.drawable.background_dialog))
            setAdapter(searchBarAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val place = searchBarAdapter.getItem(position)
                if (place != null && place is SearchBarItem.Place) {
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
            when {
                isLocationPermissionGranted() -> viewModel.updateMyLocationConfig(isFollowLocationEnabled = true)
                shouldShowLocationRationale() -> showLocationRationaleDialog()
                else -> permissionLauncher.launch(locationPermissions)
            }
        }

        homeRoutesNearbyFab.setOnClickListener {
            viewModel.loadHikingRoutes(
                placeName = getString(R.string.map_place_name_routes_nearby),
                boundingBox = homeMapView.boundingBox.toDomainBoundingBox()
            )
        }

        homeLayersFab.setOnClickListener { layersPopupWindow.show(it) }
    }

    private fun showLocationRationaleDialog() {
        MaterialAlertDialogBuilder(this@HomeActivity, R.style.DefaultMaterialDialog)
            .setTitle(R.string.my_location_rationale_title)
            .setMessage(R.string.my_location_rationale_message)
            .setNegativeButton(R.string.my_location_rationale_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.my_location_rationale_positive_button) { _, _ ->
                permissionLauncher.launch(locationPermissions)
            }
            .show()
    }

    private fun initBottomSheets() {
        placeDetailsSheet = BottomSheetBehavior.from(binding.homePlaceDetailsBottomSheetContainer.root)
        placeDetailsSheet.hide()

        hikingRoutesSheet = BottomSheetBehavior.from(binding.homeHikingRoutesBottomSheetContainer.root)
        hikingRoutesSheet.hide()
    }

    private fun initPermissions() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.isLocationPermissionGranted() -> {
                    viewModel.updateMyLocationConfig(isFollowLocationEnabled = true)
                }
            }
        }
    }

    private fun updateMyLocationConfig(myLocationUiModel: MyLocationUiModel) {
        val locationOverlay = myLocationOverlay ?: return

        when {
            myLocationUiModel.isFollowLocationEnabled && !locationOverlay.isFollowLocationEnabled -> {
                locationOverlay.enableFollowLocation()
            }
            !myLocationUiModel.isFollowLocationEnabled && locationOverlay.isFollowLocationEnabled -> {
                myLocationOverlay?.disableFollowLocation()
            }
        }
    }

    private fun enableMyLocationMonitoring() {
        homeMyLocationButton.setImageResource(R.drawable.ic_anim_home_fab_my_location_not_fixed)
        homeMyLocationButton.startDrawableAnimation()

        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationOverlay(lifecycleScope, myLocationProvider, homeMapView).apply {
                runOnFirstFix {
                    if (!isFollowLocationEnabled) {
                        homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)
                    }
                }
                onFollowLocationFirstFix = {
                    homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                }
                onFollowLocationDisabled = {
                    homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)

                    viewModel.updateMyLocationConfig(isFollowLocationEnabled = false)
                }
                homeMapView.addOverlay(OverlayPositions.MY_LOCATION, this)
            }
        }

        lifecycleScope.launch {
            myLocationOverlay!!.enableMyLocationFlow()
                .distinctUntilChanged()
                .onEach { viewModel.loadLandscapes(it) }
                .collect()
        }
    }

    private fun initOfflineLayer(file: File) {
        with(homeMapView) {
            val offlineProvider = OsmAndOfflineTileProvider(SimpleRegisterReceiver(this@HomeActivity), file)

            offlineProvider.tileRequestCompleteHandlers.apply {
                // Issue: https://github.com/osmdroid/osmdroid/issues/690
                clear()
                add(tileRequestCompleteHandler)
            }
            val overlay = TilesOverlay(offlineProvider, applicationContext).apply {
                loadingBackgroundColor = Color.TRANSPARENT
                loadingLineColor = Color.TRANSPARENT
            }
            addOverlay(OverlayPositions.HIKING_LAYER, overlay)
        }
    }

    @Suppress("LongMethod")
    private fun initFlows() {
        lifecycleScope.launch {
            viewModel.mapUiModel
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { mapUiModel ->
                    homeMapView.zoomToBoundingBox(mapUiModel.boundingBox.toOsmBoundingBox(), false)
                }
        }
        lifecycleScope.launch {
            viewModel.hikingLayer
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { initHikingLayerState(it) }
        }
        lifecycleScope.launch {
            viewModel.myLocationUiModel
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { myLocationUiModel ->
                    updateMyLocationConfig(myLocationUiModel)
                }
        }
        lifecycleScope.launch {
            viewModel.placeDetails
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { placeDetailsUiModel ->
                    placeDetailsUiModel?.let { initPlaceDetails(it) }
                }
        }
        lifecycleScope.launch {
            viewModel.searchBarItems
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { searchBarItems ->
                    searchBarItems?.let { initSearchBarItems(it) }
                }
        }
        lifecycleScope.launch {
            viewModel.landscapes
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { landscapes ->
                    landscapes?.let { initLandscapes(it) }
                }
        }
        lifecycleScope.launch {
            viewModel.hikingRoutes
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { hikingRoutes ->
                    hikingRoutes?.let { initHikingRoutes(it) }
                }
        }
        lifecycleScope.launch {
            viewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    showSnackbar(homeContainer, errorMessage)
                }
        }
        lifecycleScope.launch {
            viewModel.loading
                .flowWithLifecycle(lifecycle)
                .collect { isLoading ->
                    binding.homeSearchBarProgress.visibleOrGone(isLoading)
                }
        }
    }

    private fun initHikingLayerState(hikingLayerState: HikingLayerUiModel) {
        when (hikingLayerState) {
            is HikingLayerUiModel.NotDownloaded -> {
                homeLayersFab.visible()
                layersPopupWindow.show(homeLayersFab)
            }
            is HikingLayerUiModel.Downloaded -> {
                homeLayersFab.visible()
                initOfflineLayer(hikingLayerState.hikingLayerFile)
            }
            is HikingLayerUiModel.Loading -> homeLayersFab.gone()
            else -> {
                // No-op
            }
        }

        layersPopupWindow.updateDialog(hikingLayerState,
            onDownloadButtonClick = { viewModel.downloadHikingLayer() }
        )
    }

    private fun initSearchBarItems(placeItems: List<SearchBarItem>) {
        when {
            placeItems.all { it is SearchBarItem.Place } -> {
                searchBarAdapter.submitList(placeItems)
                searchBarPopup.apply {
                    width = homeSearchBarPopupAnchor.width
                    height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
                    show()
                }
            }
            placeItems.size == 1 && placeItems.all { it is SearchBarItem.Error } -> {
                searchBarAdapter.submitList(placeItems)
                searchBarPopup.apply {
                    width = homeSearchBarPopupAnchor.width
                    height = ListPopupWindow.WRAP_CONTENT
                    show()
                }
            }
        }
    }

    private fun initLandscapes(landscapes: List<PlaceUiModel>) {
        binding.homeLandscapeChipGroup.removeAllViews()

        landscapes.forEach { landscape ->
            val chipBinding = ItemHomeLandscapesChipBinding.inflate(
                layoutInflater,
                binding.homeLandscapeChipGroup,
                false
            )
            with(chipBinding.landscapesChip) {
                text = landscape.primaryText
                setChipIconResource(landscape.iconRes)
                setOnClickListener {
                    viewModel.loadPlaceDetails(landscape)
                }
            }
            binding.homeLandscapeChipGroup.addView(chipBinding.root)
        }
    }

    private fun initPlaceDetails(placeDetails: PlaceDetailsUiModel) {
        when (placeDetails.geometryUiModel) {
            is GeometryUiModel.Node -> initNodeDetails(placeDetails.placeUiModel)
            is GeometryUiModel.Way -> initWayDetails(placeDetails)
            is GeometryUiModel.Relation -> initRelationDetails(placeDetails)
        }
    }

    private fun initNodeDetails(placeUiModel: PlaceUiModel) {
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

    private fun initWayDetails(placeDetails: PlaceDetailsUiModel) {
        val geometryUiModel = placeDetails.geometryUiModel as? GeometryUiModel.Way ?: return

        val geoPoints = geometryUiModel.geoPoints
        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
        val boundingBoxWithOffset = boundingBox.withDefaultOffset()
        val polyOverlay = if (geometryUiModel.isClosed) {
            homeMapView.addPolygon(
                geoPoints = geoPoints,
                onClick = { polygon ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polygon))
                    placeDetailsSheet.collapse()
                    homeMapView.zoomToBoundingBox(boundingBoxWithOffset, true)
                }
            )
        } else {
            homeMapView.addPolyline(
                geoPoints = geoPoints,
                onClick = { polyline ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyline))
                    placeDetailsSheet.collapse()
                    homeMapView.zoomToBoundingBox(boundingBoxWithOffset, true)
                }
            )
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyOverlay))
        placeDetailsSheet.collapse()

        homeMapView.zoomToBoundingBox(boundingBoxWithOffset, true)
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

    private fun initHikingRoutes(hikingRoutes: List<HikingRoutesItem>) {
        with(binding.homeHikingRoutesBottomSheetContainer) {
            val hikingRoutesAdapter = HikingRoutesAdapter(
                onItemClick = { hikingRoute ->
                    hikingRoutesSheet.hide()
                    viewModel.loadHikingRouteDetails(hikingRoute)
                    homeMapView.removePlaceOverlays()
                },
                onCloseClick = {
                    hikingRoutesSheet.hide()

                    viewModel.clearHikingRoutes()
                }
            )
            hikingRoutesList.setHasFixedSize(true)
            hikingRoutesList.adapter = hikingRoutesAdapter
            hikingRoutesAdapter.submitList(hikingRoutes)
            hikingRoutesEmptyView.visibleOrGone(hikingRoutes.size <= 1)

            placeDetailsSheet.hide()
            hikingRoutesSheet.collapse()
        }
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
                        placeName = getString(R.string.map_place_name_node_routes_nearby, placeUiModel.primaryText),
                        boundingBox = homeMapView.boundingBox.toDomainBoundingBox()
                    )
                }
            }
            placeDetailsCloseButton.setOnClickListener {
                homeMapView.removeMarker(marker)
                placeDetailsSheet.hide()

                viewModel.clearPlaceDetails()
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

                viewModel.loadHikingRoutes(
                    placeName = placeUiModel.primaryText,
                    boundingBox = boundingBox.toDomainBoundingBox()
                )
            }
            placeDetailsCloseButton.setOnClickListener {
                placeDetailsSheet.hide()
                if (overlays.isNotEmpty()) {
                    homeMapView.removeOverlay(overlays)
                }

                viewModel.clearPlaceDetails()
            }
        }
    }

    private fun initReceivers() {
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        layerDownloadReceiver = registerReceiver(intentFilter) { intent ->
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            viewModel.saveHikingLayer(downloadId)
        }
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
