package hu.mostoha.mobile.android.huki.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ActivityHomeBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeLandscapesChipBinding
import hu.mostoha.mobile.android.huki.extensions.OffsetType
import hu.mostoha.mobile.android.huki.extensions.addFragment
import hu.mostoha.mobile.android.huki.extensions.addGpxMarker
import hu.mostoha.mobile.android.huki.extensions.addGpxPolyline
import hu.mostoha.mobile.android.huki.extensions.addLandscapePolyOverlay
import hu.mostoha.mobile.android.huki.extensions.addLocationPickerMarker
import hu.mostoha.mobile.android.huki.extensions.addLongClickHandlerOverlay
import hu.mostoha.mobile.android.huki.extensions.addMapMovedListener
import hu.mostoha.mobile.android.huki.extensions.addMarker
import hu.mostoha.mobile.android.huki.extensions.addOverlay
import hu.mostoha.mobile.android.huki.extensions.addPolygon
import hu.mostoha.mobile.android.huki.extensions.addPolyline
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerMarker
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerPolyline
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.extensions.clearFocusAndHideKeyboard
import hu.mostoha.mobile.android.huki.extensions.generateLayerDrawable
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.hasNoOverlay
import hu.mostoha.mobile.android.huki.extensions.hasOverlay
import hu.mostoha.mobile.android.huki.extensions.hideAll
import hu.mostoha.mobile.android.huki.extensions.hideOverlay
import hu.mostoha.mobile.android.huki.extensions.isDarkMode
import hu.mostoha.mobile.android.huki.extensions.isGpxFileIntent
import hu.mostoha.mobile.android.huki.extensions.isLocationPermissionGranted
import hu.mostoha.mobile.android.huki.extensions.locationPermissions
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.removeMarker
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.resolve
import hu.mostoha.mobile.android.huki.extensions.setStatusBarColor
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.extensions.shouldShowLocationRationale
import hu.mostoha.mobile.android.huki.extensions.showOnly
import hu.mostoha.mobile.android.huki.extensions.showOverlay
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.switchOverlayVisibility
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.extensions.withOffset
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.toDomainBoundingBox
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.domain.toOsmBoundingBox
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.PickLocationState
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.tileprovider.AwsMapTileProviderBasic
import hu.mostoha.mobile.android.huki.service.FirebaseAnalyticsService
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.ui.home.gpx.GpxDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.landscapedetails.LandscapeDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.placedetails.PlaceDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderPopup
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderPopup.Companion.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.RoutePlannerFragment
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.RoutePlannerViewModel
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsViewModel
import hu.mostoha.mobile.android.huki.util.DARK_MODE_HIKING_LAYER_BRIGHTNESS
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.OSM_ID_UNAVAILABLE
import hu.mostoha.mobile.android.huki.util.getBrightnessColorMatrix
import hu.mostoha.mobile.android.huki.util.getColorScaledMatrix
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.PolyOverlayWithIW
import org.osmdroid.views.overlay.TilesOverlay
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    @Inject
    lateinit var myLocationProvider: AsyncMyLocationProvider

    @Inject
    lateinit var analyticsService: FirebaseAnalyticsService

    private val homeViewModel: HomeViewModel by viewModels()
    private val layersViewModel: LayersViewModel by viewModels()
    private val placeFinderViewModel: PlaceFinderViewModel by viewModels()
    private val routePlannerViewModel: RoutePlannerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val homeContainer by lazy { binding.homeContainer }
    private val homeMapView by lazy { binding.homeMapView }
    private val homeSearchBarContainer by lazy { binding.homeSearchBarContainer }
    private val homeSearchBarInputLayout by lazy { binding.homeSearchBarInputLayout }
    private val homeSearchBarInput by lazy { binding.homeSearchBarInput }
    private val homeSearchBarPopupAnchor by lazy { binding.homeSearchBarPopupAnchor }
    private val homeHeaderGroup by lazy { binding.homeHeaderGroup }

    private val homeMyLocationButton by lazy { binding.homeMyLocationButton }
    private val homeRoutePlannerFab by lazy { binding.homeRoutePlannerFab }
    private val homeLayersFab by lazy { binding.homeLayersFab }
    private val homeSettingsFab by lazy { binding.homeSettingsFab }
    private val homeAltitudeText by lazy { binding.homeAltitudeText }

    private lateinit var placeFinderPopup: PlaceFinderPopup
    private lateinit var placeDetailsBottomSheet: PlaceDetailsBottomSheetDialog
    private lateinit var landscapeDetailsBottomSheet: LandscapeDetailsBottomSheetDialog
    private lateinit var hikingRoutesBottomSheet: HikingRoutesBottomSheetDialog
    private lateinit var gpxDetailsBottomSheet: GpxDetailsBottomSheetDialog
    private lateinit var bottomSheets: List<BottomSheetDialog>

    private var myLocationOverlay: MyLocationOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWindow()
        initViews()
        initPermissions()
        initIntentHandlers(intent)
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

        val searchBarTopMargin = homeSearchBarContainer.marginTop

        ViewCompat.setOnApplyWindowInsetsListener(homeContainer) { _, windowInsetsCompat ->
            val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())

            homeSearchBarContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(top = searchBarTopMargin + insets.top)
            }

            routePlannerViewModel.updateTopInsetSize(insets.top)

            WindowInsetsCompat.CONSUMED
        }
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
            addMapMovedListener {
                homeViewModel.clearHikingRoutes()
                hikingRoutesBottomSheet.hide()
            }
            addOverlay(OsmLicencesOverlay(this@HomeActivity, analyticsService), OverlayComparator)
            if (isDarkMode()) {
                overlayManager.tilesOverlay.setColorFilter(getColorScaledMatrix(getColor(R.color.colorScaleDarkMap)))
            }
        }
    }

    private fun initSearchBar() {
        homeSearchBarInputLayout.setEndIconOnClickListener {
            clearSearchBarInput()
        }
        homeSearchBarInput.addTextChangedListener { editable ->
            val text = editable.toString()
            if (text.length >= PLACE_FINDER_MIN_TRIGGER_LENGTH) {
                placeFinderViewModel.loadPlaces(text)
            }
        }
        placeFinderPopup = PlaceFinderPopup(
            context = this,
            onPlaceClick = { placeUiModel ->
                analyticsService.placeFinderPlaceClicked(
                    homeSearchBarInput.text.toString(),
                    placeUiModel.primaryText.resolve(this@HomeActivity)
                )

                clearSearchBarInput()

                homeViewModel.loadPlace(placeUiModel)
            },
            onMyLocationClick = {
                clearSearchBarInput()

                lifecycleScope.launch {
                    val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine() ?: return@launch
                    val lastKnownGeoPoint = lastKnownLocation.toLocation().toGeoPoint()
                    val placeUiModel = PlaceUiModel(
                        osmId = OSM_ID_UNAVAILABLE,
                        placeType = PlaceType.NODE,
                        geoPoint = lastKnownGeoPoint,
                        primaryText = LocationFormatter.format(lastKnownGeoPoint),
                        secondaryText = R.string.place_details_my_location_text.toMessage(),
                        iconRes = R.drawable.ic_place_type_node,
                    )

                    homeViewModel.loadPlace(placeUiModel)
                }
            },
            onPickLocationClick = {
                clearSearchBarInput()

                showSnackbar(
                    binding.homeContainer,
                    R.string.place_finder_pick_location_message.toMessage(),
                    R.drawable.ic_place_finder_pick_location_message
                )

                homeMapView.addLongClickHandlerOverlay { geoPoint ->
                    homeMapView.addLocationPickerMarker(
                        geoPoint = geoPoint,
                        onSaveClick = {
                            val placeUiModel = PlaceUiModel(
                                osmId = OSM_ID_UNAVAILABLE,
                                placeType = PlaceType.NODE,
                                geoPoint = geoPoint,
                                primaryText = LocationFormatter.format(geoPoint),
                                secondaryText = R.string.place_details_pick_location_text.toMessage(),
                                iconRes = R.drawable.ic_place_type_node,
                            )
                            homeViewModel.loadPlace(placeUiModel)
                        },
                    )
                }
            }
        )
    }

    private fun clearSearchBarInput() {
        homeSearchBarInput.text?.clear()
        homeSearchBarInput.clearFocusAndHideKeyboard()
        placeFinderViewModel.cancelSearch()
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

        homeRoutePlannerFab.setOnClickListener {
            analyticsService.routePlannerClicked()

            bottomSheets.hideAll()
            homeViewModel.clearAllOverlay()

            routePlannerViewModel.initWaypoints()

            supportFragmentManager.addFragment(R.id.homeRoutePlannerContainer, RoutePlannerFragment::class.java)
        }

        homeLayersFab.setOnClickListener {
            homeViewModel.clearFollowLocation()

            LayersBottomSheetDialogFragment().show(supportFragmentManager, LayersBottomSheetDialogFragment.TAG)
        }

        homeSettingsFab.setOnClickListener {
            analyticsService.settingsClicked()
            SettingsBottomSheetDialogFragment().show(supportFragmentManager, SettingsBottomSheetDialogFragment.TAG)
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
        hikingRoutesBottomSheet = HikingRoutesBottomSheetDialog(binding.homeHikingRoutesBottomSheetContainer)
        gpxDetailsBottomSheet = GpxDetailsBottomSheetDialog(binding.homeGpxDetailsBottomSheetContainer)
        placeDetailsBottomSheet = PlaceDetailsBottomSheetDialog(
            binding.homePlaceDetailsBottomSheetContainer,
            analyticsService
        )
        landscapeDetailsBottomSheet = LandscapeDetailsBottomSheetDialog(
            binding.homeLandscapeDetailsBottomSheetContainer,
            analyticsService
        )

        bottomSheets = listOf(
            placeDetailsBottomSheet,
            hikingRoutesBottomSheet,
            gpxDetailsBottomSheet,
            landscapeDetailsBottomSheet
        )

        placeDetailsBottomSheet.hide()
        landscapeDetailsBottomSheet.hide()
        hikingRoutesBottomSheet.hide()
        gpxDetailsBottomSheet.hide()
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
                        postMain {
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

    private fun enableFollowingLocation(isEnabled: Boolean) {
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

    private fun initFlows() {
        initHomeFlows()
        initLayersFlows()
        initPlaceFinderFlows()
        initRoutePlannerFlows()
        initSettingsFlows()
    }

    private fun initHomeFlows() {
        lifecycleScope.launch {
            homeViewModel.mapUiModel
                .flowWithLifecycle(lifecycle)
                .collect { mapUiModel ->
                    val boundingBox = if (mapUiModel.withDefaultOffset) {
                        mapUiModel.boundingBox.toOsmBoundingBox()
                    } else {
                        mapUiModel.boundingBox
                            .toOsmBoundingBox()
                            .withOffset(homeMapView, OffsetType.DEFAULT)
                    }
                    homeMapView.zoomToBoundingBox(boundingBox, false)
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationUiModel
                .flowWithLifecycle(lifecycle)
                .collect { myLocationUiModel ->
                    if (myLocationUiModel.isLocationPermissionEnabled) {
                        enableMyLocationMonitoring(myLocationUiModel.isAnimationEnabled)
                        enableFollowingLocation(myLocationUiModel.isFollowLocationEnabled)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.placeDetails
                .flowWithLifecycle(lifecycle)
                .collect { initPlaceDetails(it) }
        }
        lifecycleScope.launch {
            homeViewModel.landscapes
                .flowWithLifecycle(lifecycle)
                .collect { landscapes ->
                    landscapes?.let { initLandscapes(it) }
                }
        }
        lifecycleScope.launch {
            homeViewModel.landscapeDetails
                .flowWithLifecycle(lifecycle)
                .collect { initLandscapeDetails(it) }
        }
        lifecycleScope.launch {
            homeViewModel.hikingRoutes
                .flowWithLifecycle(lifecycle)
                .collect { hikingRoutes ->
                    hikingRoutes?.let { initHikingRoutesBottomSheet(it) }
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

    private fun initLayersFlows() {
        lifecycleScope.launch {
            layersViewModel.layersConfig
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .map { it.baseLayer }
                .collect { baseLayer ->
                    homeMapView.setTileSource(baseLayer.tileSource)
                }
        }
        lifecycleScope.launch {
            layersViewModel.layersConfig
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .map { it.hikingLayer }
                .collect { layerSpec ->
                    initHikingLayer(layerSpec)
                }
        }
        lifecycleScope.launch {
            layersViewModel.gpxDetailsUiModel
                .flowWithLifecycle(lifecycle)
                .collect { gpxUiModel ->
                    initGpxDetails(gpxUiModel)
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.routePlanUiModel
                .flowWithLifecycle(lifecycle)
                .collect { routePlanUiModel ->
                    initRoutePlan(routePlanUiModel)
                }
        }
    }

    private fun initRoutePlannerFlows() {
        lifecycleScope.launch {
            routePlannerViewModel.pickLocationState
                .flowWithLifecycle(lifecycle)
                .collect { pickLocationState ->
                    pickLocationState?.let { state ->
                        if (state == PickLocationState.Started) {
                            homeMapView.addLongClickHandlerOverlay { geoPoint ->
                                homeMapView.addLocationPickerMarker(
                                    geoPoint = geoPoint,
                                    onSaveClick = {
                                        routePlannerViewModel.savePickedLocation(geoPoint)
                                    },
                                    onCloseClick = {
                                        routePlannerViewModel.clearPickedLocation()
                                    }
                                )
                            }
                        }
                    }
                }
        }
        lifecycleScope.launch {
            routePlannerViewModel.waypointItems
                .flowWithLifecycle(lifecycle)
                .collect { waypointItems ->
                    postMain {
                        if (waypointItems.isEmpty()) {
                            homeHeaderGroup.visible()
                            homeRoutePlannerFab.show()
                        } else {
                            homeHeaderGroup.gone()
                            homeRoutePlannerFab.hide()
                        }
                    }
                }
        }
    }

    private fun initPlaceFinderFlows() {
        lifecycleScope.launch {
            placeFinderViewModel.placeFinderItems
                .flowWithLifecycle(lifecycle)
                .collect { placeFinderItems ->
                    placeFinderItems?.let { items ->
                        placeFinderPopup.initPlaceFinderItems(homeSearchBarPopupAnchor, items)
                    }
                }
        }
    }

    private fun initSettingsFlows() {
        lifecycleScope.launch {
            settingsViewModel.mapScaleFactor
                .flowWithLifecycle(lifecycle)
                .collect { mapScaleFactor ->
                    homeMapView.tilesScaleFactor = mapScaleFactor.toFloat()
                    homeMapView.invalidate()
                }
        }
    }

    private fun initIntentHandlers(intent: Intent?) {
        if (intent != null && intent.isGpxFileIntent()) {
            lifecycleScope.launch {
                homeViewModel.clearFollowLocation()

                layersViewModel.loadGpx(intent.data)

                analyticsService.gpxImportedByIntent()
            }
        }
    }

    private fun initHikingLayer(hikingLayer: HikingLayer?) {
        removeHikingLayer()

        if (hikingLayer != null) {
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
    }

    private fun removeHikingLayer() {
        homeMapView.overlays.removeIf { it is TilesOverlay }
        homeMapView.invalidate()
    }

    private fun initLandscapes(landscapes: List<LandscapeUiModel>) {
        binding.homeLandscapeChipGroup.removeAllViews()

        landscapes.forEach { landscape ->
            val chipBinding = ItemHomeLandscapesChipBinding.inflate(
                layoutInflater,
                binding.homeLandscapeChipGroup,
                false
            )

            with(chipBinding.landscapesChip) {
                text = landscape.name.resolve(chipBinding.root.context)
                setChipIconResource(landscape.iconRes)
                setOnClickListener {
                    analyticsService.loadLandscapeClicked(landscape.name.resolve(chipBinding.root.context))
                    homeViewModel.loadLandscapeDetails(landscape)
                }
            }
            binding.homeLandscapeChipGroup.addView(chipBinding.root)
        }
    }

    private fun initLandscapeDetails(landscapeDetails: LandscapeDetailsUiModel?) {
        when {
            landscapeDetails == null -> {
                homeMapView.removeOverlay(OverlayType.LANDSCAPE)
            }
            homeMapView.hasNoOverlay(landscapeDetails.landscapeUiModel.osmId) -> {
                homeMapView.removeOverlay(OverlayType.LANDSCAPE)

                val osmId = landscapeDetails.landscapeUiModel.osmId
                val geometryUiModel = landscapeDetails.geometryUiModel
                val boundingBox = BoundingBox.fromGeoPoints(geometryUiModel.ways.flatMap { it.geoPoints })
                val offsetBoundingBox = boundingBox.withOffset(homeMapView, OffsetType.BOTTOM_SHEET)
                val overlays = mutableListOf<OverlayWithIW>()

                geometryUiModel.ways.forEach { way ->
                    val landscapeOverlays = homeMapView.addLandscapePolyOverlay(
                        overlayId = osmId,
                        center = landscapeDetails.landscapeUiModel.geoPoint,
                        centerMarkerDrawable = generateLayerDrawable(
                            layers = listOf(
                                R.drawable.ic_marker_polygon_background.toDrawable(this),
                                landscapeDetails.landscapeUiModel.markerRes.toDrawable(this),
                            ),
                            padding = resources.getDimensionPixelSize(R.dimen.space_small)
                        ),
                        way = way,
                        onClick = {
                            initLandscapeDetailsBottomSheet(landscapeDetails, boundingBox)

                            homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                        }
                    )
                    overlays.addAll(landscapeOverlays)
                }

                initLandscapeDetailsBottomSheet(landscapeDetails, boundingBox)

                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            }
        }
    }

    private fun initPlaceDetails(placeDetails: PlaceDetailsUiModel?) {
        when {
            placeDetails == null -> {
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)
            }
            homeMapView.hasNoOverlay(placeDetails.placeUiModel.osmId) -> {
                when (placeDetails.geometryUiModel) {
                    is GeometryUiModel.Node -> initNodeDetails(placeDetails.placeUiModel)
                    is GeometryUiModel.Way -> initWayDetails(placeDetails)
                    is GeometryUiModel.Relation -> initRelationDetails(placeDetails)
                }
            }
        }
    }

    private fun initNodeDetails(placeUiModel: PlaceUiModel) {
        val geoPoint = placeUiModel.geoPoint
        val boundingBox = placeUiModel.boundingBox

        val marker = homeMapView.addMarker(
            overlayId = placeUiModel.osmId,
            geoPoint = geoPoint,
            iconDrawable = R.drawable.ic_marker_poi.toDrawable(this),
            onClick = { marker ->
                initNodeBottomSheet(placeUiModel, marker)

                if (boundingBox != null) {
                    val offsetBoundingBox = boundingBox
                        .toOsmBoundingBox()
                        .withOffset(homeMapView, OffsetType.BOTTOM_SHEET)
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                } else {
                    homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
                }
            }
        )

        initNodeBottomSheet(placeUiModel, marker)

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
        val offsetBoundingBox = boundingBox.withOffset(homeMapView, OffsetType.BOTTOM_SHEET)
        val polyOverlay = if (geometryUiModel.isClosed) {
            homeMapView.addPolygon(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = geoPoints,
                onClick = { polygon ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polygon))
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        } else {
            homeMapView.addPolyline(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = geoPoints,
                onClick = { polyline ->
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyline))
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, listOf(polyOverlay))

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
    }

    private fun initRelationDetails(placeDetails: PlaceDetailsUiModel) {
        val relation = placeDetails.geometryUiModel as? GeometryUiModel.Relation ?: return
        val boundingBox = BoundingBox
            .fromGeoPoints(relation.ways.flatMap { it.geoPoints })
            .withOffset(homeMapView, OffsetType.BOTTOM_SHEET)

        val overlays = mutableListOf<PolyOverlayWithIW>()

        relation.ways.forEach { way ->
            val geoPoints = way.geoPoints
            val overlay = homeMapView.addPolyline(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = geoPoints,
                onClick = {
                    initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)
                    homeMapView.zoomToBoundingBox(boundingBox, true)
                }
            )

            overlays.add(overlay)
        }

        initWayBottomSheet(placeDetails.placeUiModel, boundingBox, overlays)

        homeMapView.zoomToBoundingBox(boundingBox, true)
    }

    private fun initHikingRoutesBottomSheet(hikingRoutes: List<HikingRoutesItem>) {
        hikingRoutesBottomSheet.initBottomSheet(
            hikingRoutes = hikingRoutes,
            onHikingRouteClick = { hikingRoute ->
                homeViewModel.loadHikingRouteDetails(hikingRoute)
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)
                analyticsService.loadHikingRouteDetailsClicked(hikingRoute.name)
            },
            onCloseClick = { homeViewModel.clearHikingRoutes() }
        )
        bottomSheets.showOnly(hikingRoutesBottomSheet)
    }

    private fun initNodeBottomSheet(placeUiModel: PlaceUiModel, marker: Marker) {
        placeDetailsBottomSheet.initNodeBottomSheet(
            placeUiModel = placeUiModel,
            onShowAllPointsClick = {
                placeDetailsBottomSheet.hide()
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)

                homeViewModel.loadPlaceDetails(placeUiModel)
            },
            onRoutePlanButtonClick = {
                placeDetailsBottomSheet.hide()
                homeMapView.removeMarker(marker)
                homeViewModel.clearPlaceDetails()

                routePlannerViewModel.initWaypoints(placeUiModel)

                supportFragmentManager.addFragment(R.id.homeRoutePlannerContainer, RoutePlannerFragment::class.java)
            },
            onCloseButtonClick = {
                placeDetailsBottomSheet.hide()
                homeMapView.removeMarker(marker)

                homeViewModel.clearPlaceDetails()
            }
        )
        bottomSheets.showOnly(placeDetailsBottomSheet)
    }

    private fun initWayBottomSheet(place: PlaceUiModel, boundingBox: BoundingBox, overlays: List<Overlay>) {
        placeDetailsBottomSheet.initWayBottomSheet(
            placeUiModel = place,
            onHikingTrailsButtonClick = {
                placeDetailsBottomSheet.hide()

                val placeTitle = getString(
                    R.string.map_place_name_node_routes_nearby,
                    place.primaryText.resolve(this@HomeActivity)
                )
                homeViewModel.loadHikingRoutes(placeTitle, boundingBox.toDomainBoundingBox())
            },
            onCloseButtonClick = {
                placeDetailsBottomSheet.hide()
                if (overlays.isNotEmpty()) {
                    homeMapView.removeOverlay(overlays)
                }

                homeViewModel.clearPlaceDetails()
            }
        )
        bottomSheets.showOnly(placeDetailsBottomSheet)
    }

    private fun initLandscapeDetailsBottomSheet(landscapeDetails: LandscapeDetailsUiModel, boundingBox: BoundingBox) {
        landscapeDetailsBottomSheet.initLandscapeDetailsBottomSheet(
            landscapeDetailsUiModel = landscapeDetails,
            onHikingTrailsButtonClick = {
                val placeTitle = getString(
                    R.string.map_place_name_node_routes_nearby,
                    landscapeDetails.landscapeUiModel.name.resolve(this@HomeActivity)
                )
                homeViewModel.loadHikingRoutes(placeTitle, boundingBox.toDomainBoundingBox())
                landscapeDetailsBottomSheet.hide()
            },
            onCloseButtonClick = {
                homeViewModel.clearLandscapeDetails()
                landscapeDetailsBottomSheet.hide()
            }
        )
        bottomSheets.showOnly(landscapeDetailsBottomSheet)
    }

    private fun initGpxDetails(gpxDetailsUiModel: GpxDetailsUiModel?) {
        if (gpxDetailsUiModel == null) {
            homeMapView.removeOverlay(OverlayType.GPX)
            return
        }

        if (homeMapView.hasOverlay(gpxDetailsUiModel.id)) {
            if (gpxDetailsUiModel.isVisible) {
                homeMapView.showOverlay(gpxDetailsUiModel.id)
            } else {
                homeMapView.hideOverlay(gpxDetailsUiModel.id)
            }
            return
        }

        homeMapView.removeOverlay(OverlayType.GPX)

        val offsetBoundingBox = gpxDetailsUiModel.boundingBox.withOffset(homeMapView, OffsetType.BOTTOM_SHEET)

        homeMapView.addGpxPolyline(
            overlayId = gpxDetailsUiModel.id,
            geoPoints = gpxDetailsUiModel.geoPoints,
            useAltitudeColors = gpxDetailsUiModel.altitudeUiModel != null,
            onClick = {
                gpxDetailsBottomSheet.show()
                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            }
        )

        gpxDetailsUiModel.waypoints.forEach { waypointItem ->
            homeMapView.addGpxMarker(
                overlayId = gpxDetailsUiModel.id,
                geoPoint = waypointItem.geoPoint,
                waypointType = waypointItem.waypointType,
                infoWindowTitle = waypointItem.name?.resolve(this),
                onClick = {
                    gpxDetailsBottomSheet.show()
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        }

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)

        gpxDetailsBottomSheet.initBottomSheet(
            gpxDetails = gpxDetailsUiModel,
            onCloseClick = { layersViewModel.clearGpxDetails() },
            onStartClick = {
                lifecycleScope.launch {
                    val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()
                    val lastKnownGeoPoint = lastKnownLocation?.toLocation()?.toGeoPoint()

                    val geoPoints = if (lastKnownGeoPoint != null) {
                        listOfNotNull(lastKnownGeoPoint, gpxDetailsUiModel.geoPoints.first())
                    } else {
                        gpxDetailsUiModel.geoPoints
                    }
                    val boundingBox = BoundingBox
                        .fromGeoPoints(geoPoints)
                        .withOffset(homeMapView, OffsetType.DEFAULT)

                    homeMapView.zoomToBoundingBox(boundingBox, true)
                }
            },
            onHideClick = {
                homeMapView.switchOverlayVisibility<GpxPolyline>(gpxDetailsUiModel.id)
            }
        )
        bottomSheets.showOnly(gpxDetailsBottomSheet)
    }

    private fun initRoutePlan(routePlanUiModel: RoutePlanUiModel?) {
        homeMapView.removeOverlay(OverlayType.ROUTE_PLANNER)

        if (routePlanUiModel == null) {
            return
        }

        val offsetBoundingBox = routePlanUiModel.boundingBox.withOffset(homeMapView, OffsetType.TOP_SHEET)

        homeMapView.addRoutePlannerPolyline(
            overlayId = routePlanUiModel.id,
            geoPoints = routePlanUiModel.geoPoints,
            onClick = {
                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            }
        )

        routePlanUiModel.wayPoints.forEach { waypointItem ->
            homeMapView.addRoutePlannerMarker(
                overlayId = routePlanUiModel.id,
                geoPoint = waypointItem.location!!.toGeoPoint(),
                waypointType = waypointItem.waypointType,
                onClick = {
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        }

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
    }

}
