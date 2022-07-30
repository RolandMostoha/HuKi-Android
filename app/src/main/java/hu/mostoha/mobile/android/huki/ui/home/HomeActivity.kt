package hu.mostoha.mobile.android.huki.ui.home

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
import hu.mostoha.mobile.android.huki.extensions.addMarker
import hu.mostoha.mobile.android.huki.extensions.addOverlay
import hu.mostoha.mobile.android.huki.extensions.addPolygon
import hu.mostoha.mobile.android.huki.extensions.addPolyline
import hu.mostoha.mobile.android.huki.extensions.addZoomListener
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.extensions.applyTopMarginForStatusBar
import hu.mostoha.mobile.android.huki.extensions.clearFocusAndHideKeyboard
import hu.mostoha.mobile.android.huki.extensions.collapse
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.hide
import hu.mostoha.mobile.android.huki.extensions.isLocationPermissionGranted
import hu.mostoha.mobile.android.huki.extensions.locationPermissions
import hu.mostoha.mobile.android.huki.extensions.removeMarker
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.setStatusBarColor
import hu.mostoha.mobile.android.huki.extensions.shouldShowLocationRationale
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.extensions.withOffset
import hu.mostoha.mobile.android.huki.model.domain.LayerSpec
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.MyLocationUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarAdapter
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.MAP_TILES_SCALE_FACTOR
import hu.mostoha.mobile.android.huki.util.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import hu.mostoha.mobile.android.huki.util.startGoogleDirections
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
    }

    @Inject
    lateinit var myLocationProvider: AsyncMyLocationProvider

    private val homeViewModel: HomeViewModel by viewModels()
    private val layersViewModel: LayersViewModel by viewModels()

    private lateinit var binding: ActivityHomeBinding
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // State restoration is disabled in hiking routes because of BottomSheet-RecyclerView bug
        homeViewModel.clearHikingRoutes()
        hikingRoutesSheet.hide()
    }

    override fun onResume() {
        super.onResume()

        if (isLocationPermissionGranted()) {
            homeViewModel.updateMyLocationConfig(isLocationPermissionEnabled = true)
        }

        homeMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        myLocationOverlay?.disableMyLocation()
        homeViewModel.saveBoundingBox(homeMapView.boundingBox.toDomainBoundingBox())

        homeMapView.onPause()
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)
        homeSearchBarContainer.applyTopMarginForStatusBar(this)
    }

    private fun initViews() {
        initMapView()
        initSearchBar()
        initFabs()
        initBottomSheets()
    }

    private fun initMapView() {
        homeMapView.apply {
            tilesScaleFactor = MAP_TILES_SCALE_FACTOR
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            addOnFirstLayoutListener { _, _, _, _, _ ->
                initFlows()
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
            homeViewModel.cancelSearch()
        }
        homeSearchBarInput.addTextChangedListener { editable ->
            val text = editable.toString()
            if (text.length >= SEARCH_BAR_MIN_TRIGGER_LENGTH) {
                homeViewModel.loadSearchBarPlaces(text)
            }
        }
        searchBarPopup = ListPopupWindow(this).apply {
            anchorView = homeSearchBarPopupAnchor
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_popup_height)
            setBackgroundDrawable(ContextCompat.getDrawable(this@HomeActivity, R.drawable.background_dialog))
            setAdapter(searchBarAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val place = searchBarAdapter.getItem(position)
                if (place != null && place is SearchBarItem.Place) {
                    homeSearchBarInput.text?.clear()
                    homeSearchBarInput.clearFocusAndHideKeyboard()
                    searchBarPopup.dismiss()

                    homeViewModel.loadPlace(place.placeUiModel)
                }
            }
        }
    }

    private fun initFabs() {
        homeMyLocationButton.setOnClickListener {
            when {
                isLocationPermissionGranted() -> {
                    homeViewModel.updateMyLocationConfig(
                        isLocationPermissionEnabled = true,
                        isFollowLocationEnabled = true
                    )
                }
                shouldShowLocationRationale() -> showLocationRationaleDialog()
                else -> permissionLauncher.launch(locationPermissions)
            }
        }

        homeRoutesNearbyFab.setOnClickListener {
            homeViewModel.loadHikingRoutes(
                placeName = getString(R.string.map_place_name_routes_nearby),
                boundingBox = homeMapView.boundingBox.toDomainBoundingBox()
            )
        }

        homeLayersFab.setOnClickListener {
            LayersBottomSheetDialogFragment().show(supportFragmentManager, LayersBottomSheetDialogFragment.TAG)
        }
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
                    homeViewModel.updateMyLocationConfig(
                        isLocationPermissionEnabled = true,
                        isFollowLocationEnabled = true
                    )
                }
            }
        }
    }

    private fun enableMyLocationMonitoring() {
        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationOverlay(lifecycleScope, myLocationProvider, homeMapView).apply {
                runOnFirstFix {
                    if (isFollowLocationEnabled) {
                        homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                    } else {
                        homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)
                    }
                }
                onFollowLocationFirstFix = {
                    homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                }
                onFollowLocationDisabled = {
                    homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)

                    homeViewModel.updateMyLocationConfig(isFollowLocationEnabled = false)
                }
                homeMapView.addOverlay(OverlayPositions.MY_LOCATION, this)
            }
        }

        lifecycleScope.launch {
            myLocationOverlay!!.enableMyLocationFlow()
                .distinctUntilChanged()
                .onEach { homeViewModel.loadLandscapes(it) }
                .collect()
        }
    }

    private fun updateFollowingLocation(myLocationUiModel: MyLocationUiModel) {
        val locationOverlay = myLocationOverlay

        if (locationOverlay == null || !myLocationUiModel.isLocationPermissionEnabled) {
            return
        }

        when {
            myLocationUiModel.isFollowLocationEnabled && !locationOverlay.isFollowLocationEnabled -> {
                homeMyLocationButton.setImageResource(R.drawable.ic_anim_home_fab_my_location_not_fixed)
                homeMyLocationButton.startDrawableAnimation()

                locationOverlay.enableFollowLocation()
            }
            !myLocationUiModel.isFollowLocationEnabled && locationOverlay.isFollowLocationEnabled -> {
                locationOverlay.disableFollowLocation()
            }
        }
    }

    @Suppress("LongMethod")
    private fun initFlows() {
        lifecycleScope.launch {
            homeViewModel.mapUiModel
                .flowWithLifecycle(lifecycle)
                .collect { mapUiModel ->
                    homeMapView.zoomToBoundingBox(
                        if (mapUiModel.withDefaultOffset) {
                            mapUiModel.boundingBox.toOsmBoundingBox()
                        } else {
                            mapUiModel.boundingBox.toOsmBoundingBox().withDefaultOffset()
                        },
                        false
                    )
                }
        }
        lifecycleScope.launch {
            layersViewModel.layersConfig
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .map { it.baseLayer }
                .collect { homeMapView.setTileSource(it.tileSource) }
        }
        lifecycleScope.launch {
            layersViewModel.layersConfig
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .map { it.hikingLayer }
                .collect { layerSpec ->
                    if (layerSpec == null) {
                        removeHikingLayer()
                    } else {
                        addHikingLayer(layerSpec)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationUiModel
                .flowWithLifecycle(lifecycle)
                .collect { myLocationUiModel ->
                    if (myLocationUiModel.isLocationPermissionEnabled) {
                        enableMyLocationMonitoring()
                    }

                    updateFollowingLocation(myLocationUiModel)
                }
        }
        lifecycleScope.launch {
            homeViewModel.placeDetails
                .flowWithLifecycle(lifecycle)
                .collect { placeDetailsUiModel ->
                    placeDetailsUiModel?.let { initPlaceDetails(it) }
                }
        }
        lifecycleScope.launch {
            homeViewModel.searchBarItems
                .flowWithLifecycle(lifecycle)
                .collect { searchBarItems ->
                    searchBarItems?.let { initSearchBarItems(it) }
                }
        }
        lifecycleScope.launch {
            homeViewModel.landscapes
                .flowWithLifecycle(lifecycle)
                .collect { landscapes ->
                    landscapes?.let { initLandscapes(it) }
                }
        }
        lifecycleScope.launch {
            homeViewModel.hikingRoutes
                .flowWithLifecycle(lifecycle)
                .collect { hikingRoutes ->
                    hikingRoutes?.let { initHikingRoutes(it) }
                }
        }
        lifecycleScope.launch {
            homeViewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    showSnackbar(homeContainer, errorMessage)
                }
        }
        lifecycleScope.launch {
            homeViewModel.loading
                .flowWithLifecycle(lifecycle)
                .collect { isLoading ->
                    binding.homeSearchBarProgress.visibleOrGone(isLoading)
                }
        }
    }

    private fun addHikingLayer(hikingLayer: LayerSpec) {
        val tileProvider = MapTileProviderBasic(applicationContext).apply {
            tileSource = hikingLayer.tileSource
            tileRequestCompleteHandlers.apply {
                // Issue: https://github.com/osmdroid/osmdroid/issues/690
                clear()
                add(homeMapView.tileRequestCompleteHandler)
            }
        }

        val tilesOverlay = TilesOverlay(tileProvider, baseContext).apply {
            loadingBackgroundColor = Color.TRANSPARENT
            loadingLineColor = Color.TRANSPARENT
        }

        homeMapView.addOverlay(OverlayPositions.HIKING_LAYER, tilesOverlay)
    }

    private fun removeHikingLayer() {
        homeMapView.overlays.removeIf { it is TilesOverlay }
        homeMapView.invalidate()
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
                    homeViewModel.loadPlaceDetails(landscape)
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
                    homeViewModel.loadHikingRouteDetails(hikingRoute)
                    removePlaceOverlays()
                },
                onCloseClick = {
                    hikingRoutesSheet.hide()

                    homeViewModel.clearHikingRoutes()
                }
            )
            hikingRoutesList.setHasFixedSize(true)
            hikingRoutesList.adapter = hikingRoutesAdapter
            hikingRoutesList.visibleOrGone(hikingRoutes.size > 1)
            hikingRoutesEmptyView.visibleOrGone(hikingRoutes.size <= 1)
            hikingRoutesAdapter.submitList(hikingRoutes)

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
                    removePlaceOverlays()

                    homeViewModel.loadPlaceDetails(placeUiModel)
                }
            } else {
                placeDetailsShowPointsButton.gone()
                placeDetailsHikingTrailsButton.visible()
                placeDetailsHikingTrailsButton.setOnClickListener {
                    placeDetailsSheet.hide()

                    homeViewModel.loadHikingRoutes(
                        placeName = getString(R.string.map_place_name_node_routes_nearby, placeUiModel.primaryText),
                        boundingBox = homeMapView.boundingBox.toDomainBoundingBox()
                    )
                }
            }
            placeDetailsCloseButton.setOnClickListener {
                homeMapView.removeMarker(marker)
                placeDetailsSheet.hide()

                homeViewModel.clearPlaceDetails()
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

                homeViewModel.loadHikingRoutes(
                    placeName = placeUiModel.primaryText,
                    boundingBox = boundingBox.toDomainBoundingBox()
                )
            }
            placeDetailsCloseButton.setOnClickListener {
                placeDetailsSheet.hide()
                if (overlays.isNotEmpty()) {
                    homeMapView.removeOverlay(overlays)
                }

                homeViewModel.clearPlaceDetails()
            }
        }
    }

    private fun removePlaceOverlays() {
        homeMapView.overlays.removeIf { it !is TilesOverlay && it !is MyLocationOverlay }
        homeMapView.invalidate()
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
