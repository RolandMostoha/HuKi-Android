package hu.mostoha.mobile.android.huki.ui.home

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import hu.mostoha.mobile.android.huki.extensions.isDarkMode
import hu.mostoha.mobile.android.huki.extensions.isLocationPermissionGranted
import hu.mostoha.mobile.android.huki.extensions.locationPermissions
import hu.mostoha.mobile.android.huki.extensions.removeMarker
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.setMessageOrGone
import hu.mostoha.mobile.android.huki.extensions.setStatusBarColor
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.extensions.shouldShowLocationRationale
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.startGoogleMapsDirectionsIntent
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.extensions.withDefaultOffset
import hu.mostoha.mobile.android.huki.model.domain.LayerSpec
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.tileprovider.AwsMapTileProviderBasic
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import hu.mostoha.mobile.android.huki.ui.home.contact.ContactBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesAdapter
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarAdapter
import hu.mostoha.mobile.android.huki.ui.home.searchbar.SearchBarItem
import hu.mostoha.mobile.android.huki.ui.util.DARK_MODE_HIKING_LAYER_BRIGHTNESS
import hu.mostoha.mobile.android.huki.ui.util.getBrightnessColorMatrix
import hu.mostoha.mobile.android.huki.ui.util.getColorScaledMatrix
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.MAP_ZOOM_THRESHOLD_ROUTES_NEARBY
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.PolyOverlayWithIW
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    companion object {
        private const val SEARCH_BAR_MIN_TRIGGER_LENGTH = 3
    }

    @Inject
    lateinit var myLocationProvider: AsyncMyLocationProvider

    @Inject
    lateinit var analyticsService: FirebaseAnalyticsService

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
    private val homeInfoFab by lazy { binding.homeContactFab }
    private val homeAltitudeText by lazy { binding.homeAltitudeText }

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
        myLocationOverlay?.disableMyLocation()
        homeViewModel.saveBoundingBox(homeMapView.boundingBox.toDomainBoundingBox())

        homeMapView.onPause()

        super.onPause()
    }

    override fun onStop() {
        analyticsService.destroyed()

        super.onStop()
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
            addOverlay(OsmLicencesOverlay(this@HomeActivity, analyticsService), OverlayComparator)

            if (isDarkMode()) {
                overlayManager.tilesOverlay.setColorFilter(getColorScaledMatrix(getColor(R.color.colorScaleDarkMap)))
            }
        }
    }

    private fun initSearchBar() {
        searchBarAdapter = SearchBarAdapter(this@HomeActivity)
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
        searchBarPopup = ListPopupWindow(this@HomeActivity).apply {
            anchorView = homeSearchBarPopupAnchor
            verticalOffset = resources.getDimensionPixelSize(R.dimen.space_extra_extra_small)
            height = resources.getDimensionPixelSize(R.dimen.home_search_bar_vertical_offset)
            setBackgroundDrawable(ContextCompat.getDrawable(this@HomeActivity, R.drawable.background_dialog))
            setAdapter(searchBarAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val place = searchBarAdapter.getItem(position)
                if (place != null && place is SearchBarItem.Place) {
                    val searchText = homeSearchBarInput.text.toString()
                    val placeUiModel = place.placeUiModel

                    homeSearchBarInput.text?.clear()
                    homeSearchBarInput.clearFocusAndHideKeyboard()
                    searchBarPopup.dismiss()

                    homeViewModel.loadPlace(placeUiModel)

                    analyticsService.searchBarPlaceClicked(
                        searchText,
                        placeUiModel.primaryText.resolve(this@HomeActivity)
                    )
                }
            }
        }
    }

    private fun initFabs() {
        homeMyLocationButton.setOnClickListener {
            analyticsService.myLocationClicked()

            when {
                isLocationPermissionGranted() -> {
                    homeViewModel.updateMyLocationConfig(
                        isLocationPermissionEnabled = true,
                        isFollowLocationEnabled = true,
                        isAnimationEnabled = true
                    )
                }
                shouldShowLocationRationale() -> showLocationRationaleDialog()
                else -> permissionLauncher.launch(locationPermissions)
            }
        }

        homeRoutesNearbyFab.setOnClickListener {
            val placeName = getString(R.string.map_place_name_routes_nearby)
            val boundingBox = homeMapView.boundingBox.toDomainBoundingBox()

            homeViewModel.loadHikingRoutes(placeName, boundingBox)
            analyticsService.loadHikingRoutesClicked(placeName)
        }

        homeLayersFab.setOnClickListener {
            LayersBottomSheetDialogFragment().show(supportFragmentManager, LayersBottomSheetDialogFragment.TAG)
        }

        homeInfoFab.setOnClickListener {
            ContactBottomSheetDialogFragment().show(supportFragmentManager, ContactBottomSheetDialogFragment.TAG)
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
                        isFollowLocationEnabled = true,
                        isAnimationEnabled = true
                    )
                }
            }
        }
    }

    private fun enableMyLocationMonitoring(isAnimationEnabled: Boolean) {
        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationOverlay(lifecycleScope, myLocationProvider, homeMapView)
                .apply {
                    runOnFirstFix {
                        Handler(Looper.getMainLooper()).post {
                            if (isFollowLocationEnabled) {
                                homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                            } else {
                                homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)
                            }
                        }
                    }
                    onFollowLocationFirstFix = {
                        homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                    }
                    onFollowLocationDisabled = {
                        homeMyLocationButton.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)

                        homeViewModel.updateMyLocationConfig(isFollowLocationEnabled = false)
                    }
                    homeMapView.addOverlay(this, OverlayComparator)
                }
        }

        myLocationOverlay?.let { overlay ->
            overlay.isAnimationEnabled = isAnimationEnabled

            lifecycleScope.launch {
                overlay.myLocationFlow()
                    .distinctUntilChanged()
                    .onEach { location ->
                        homeViewModel.loadLandscapes(location)

                        initAltitude(location.altitude)
                    }
                    .collect()
            }
        }
    }

    private fun updateFollowingLocation(isEnabled: Boolean) {
        val locationOverlay = myLocationOverlay ?: return
        val previouslyEnabled = locationOverlay.isFollowLocationEnabled

        when {
            !previouslyEnabled && isEnabled -> {
                homeMyLocationButton.setImageResource(R.drawable.ic_anim_home_fab_my_location_not_fixed)
                homeMyLocationButton.startDrawableAnimation()

                locationOverlay.enableFollowLocation()
            }
            previouslyEnabled && !isEnabled -> {
                locationOverlay.disableFollowLocation()
            }
        }
    }

    private fun initAltitude(altitude: Double) {
        val altitudeText = if (altitude >= 1.0) {
            getString(R.string.default_distance_template_m, altitude.toInt())
        } else {
            null
        }
        homeAltitudeText.setTextOrGone(altitudeText)
    }

    @Suppress("LongMethod")
    private fun initFlows() {
        lifecycleScope.launch {
            homeViewModel.mapUiModel
                .flowWithLifecycle(lifecycle)
                .collect { mapUiModel ->
                    val boundingBox = if (mapUiModel.withDefaultOffset) {
                        mapUiModel.boundingBox.toOsmBoundingBox()
                    } else {
                        mapUiModel.boundingBox
                            .toOsmBoundingBox()
                            .withDefaultOffset(homeMapView)
                    }

                    homeMapView.zoomToBoundingBox(boundingBox, false)
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
                        initHikingLayer(layerSpec)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationUiModel
                .flowWithLifecycle(lifecycle)
                .collect { myLocationUiModel ->
                    if (myLocationUiModel.isLocationPermissionEnabled) {
                        enableMyLocationMonitoring(myLocationUiModel.isAnimationEnabled)
                        updateFollowingLocation(myLocationUiModel.isFollowLocationEnabled)
                    }
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

    private fun initHikingLayer(hikingLayer: LayerSpec) {
        val tileProvider = AwsMapTileProviderBasic(this, hikingLayer.tileSource)
        val tilesOverlay = TilesOverlay(tileProvider, baseContext).apply {
            if (this@HomeActivity.isDarkMode()) {
                setColorFilter(getBrightnessColorMatrix(DARK_MODE_HIKING_LAYER_BRIGHTNESS))
            }
            loadingBackgroundColor = Color.TRANSPARENT
            loadingLineColor = Color.TRANSPARENT
        }

        tileProvider.tileRequestCompleteHandlers.apply {
            // Issue: https://github.com/osmdroid/osmdroid/issues/690
            clear()
            add(homeMapView.tileRequestCompleteHandler)
        }
        homeMapView.addOverlay(tilesOverlay, OverlayComparator)
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
            val placeName = landscape.primaryText.resolve(chipBinding.root.context)
            with(chipBinding.landscapesChip) {
                text = placeName
                setChipIconResource(landscape.iconRes)
                setOnClickListener {
                    homeViewModel.loadPlaceDetails(landscape)
                    analyticsService.loadLandscapeClicked(placeName)
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
                    homeMapView.zoomToBoundingBox(
                        boundingBox.toOsmBoundingBox().withDefaultOffset(homeMapView),
                        true
                    )
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
        val boundingBoxWithOffset = boundingBox.withDefaultOffset(homeMapView)
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
            .withDefaultOffset(homeMapView)

        val overlays = mutableListOf<PolyOverlayWithIW>()

        relation.ways.forEach { way ->
            val geoPoints = way.geoPoints
            val overlay = homeMapView.addPolyline(geoPoints, onClick = {
                initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)
                placeDetailsSheet.collapse()
                homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(homeMapView), true)
            })

            overlays.add(overlay)
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)
        placeDetailsSheet.collapse()

        homeMapView.zoomToBoundingBox(boundingBox.withDefaultOffset(homeMapView), true)
    }

    private fun initHikingRoutes(hikingRoutes: List<HikingRoutesItem>) {
        with(binding.homeHikingRoutesBottomSheetContainer) {
            val hikingRoutesAdapter = HikingRoutesAdapter(
                onItemClick = { hikingRoute ->
                    hikingRoutesSheet.hide()
                    homeViewModel.loadHikingRouteDetails(hikingRoute)
                    removePlaceOverlays()
                    analyticsService.loadHikingRouteDetailsClicked(hikingRoute.name)
                },
                onCloseClick = {
                    hikingRoutesSheet.hide()

                    homeViewModel.clearHikingRoutes()
                }
            )
            hikingRoutesList.setHasFixedSize(true)
            hikingRoutesList.adapter = hikingRoutesAdapter
            hikingRoutesAdapter.submitList(hikingRoutes)

            placeDetailsSheet.hide()
            hikingRoutesSheet.collapse()
        }
    }

    private fun initNodeBottomSheet(placeUiModel: PlaceUiModel, geoPoint: GeoPoint, marker: Marker) {
        with(binding.homePlaceDetailsBottomSheetContainer) {
            val placeName = placeUiModel.primaryText.resolve(root.context)

            placeDetailsPrimaryText.text = placeName
            placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
            placeDetailsImage.setImageResource(placeUiModel.iconRes)
            placeDetailsDirectionsButton.visible()
            placeDetailsDirectionsButton.setOnClickListener {
                analyticsService.navigationClicked(placeName)

                startGoogleMapsDirectionsIntent(geoPoint)
            }
            if (placeUiModel.placeType != PlaceType.NODE) {
                placeDetailsHikingTrailsButton.gone()
                placeDetailsShowPointsButton.visible()
                placeDetailsShowPointsButton.setOnClickListener {
                    placeDetailsSheet.hide()
                    removePlaceOverlays()

                    homeViewModel.loadPlaceDetails(placeUiModel)
                    analyticsService.loadPlaceDetailsClicked(placeName, placeUiModel.placeType)
                }
            } else {
                placeDetailsShowPointsButton.gone()
                placeDetailsHikingTrailsButton.visible()
                placeDetailsHikingTrailsButton.setOnClickListener {
                    placeDetailsSheet.hide()

                    val placeTitle = getString(R.string.map_place_name_node_routes_nearby, placeUiModel.primaryText)
                    val boundingBox = homeMapView.boundingBox.toDomainBoundingBox()

                    homeViewModel.loadHikingRoutes(placeTitle, boundingBox)
                    analyticsService.loadHikingRoutesClicked(placeTitle)
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
            val placeName = placeUiModel.primaryText.resolve(root.context)

            placeDetailsPrimaryText.text = placeName
            placeDetailsSecondaryText.setMessageOrGone(placeUiModel.secondaryText)
            placeDetailsImage.setImageResource(placeUiModel.iconRes)
            placeDetailsDirectionsButton.gone()
            placeDetailsShowPointsButton.gone()
            placeDetailsHikingTrailsButton.visible()
            placeDetailsHikingTrailsButton.setOnClickListener {
                placeDetailsSheet.hide()

                homeViewModel.loadHikingRoutes(placeName, boundingBox.toDomainBoundingBox())
                analyticsService.loadHikingRoutesClicked(placeName)
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

}
