package hu.mostoha.mobile.android.huki.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
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
import hu.mostoha.mobile.android.huki.deeplink.DeeplinkHandler
import hu.mostoha.mobile.android.huki.extensions.OffsetType
import hu.mostoha.mobile.android.huki.extensions.PopupMenuActionItem
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.addFragment
import hu.mostoha.mobile.android.huki.extensions.addGpxMarker
import hu.mostoha.mobile.android.huki.extensions.addGpxPolyline
import hu.mostoha.mobile.android.huki.extensions.addHikingRouteDetails
import hu.mostoha.mobile.android.huki.extensions.addLandscapePolyOverlay
import hu.mostoha.mobile.android.huki.extensions.addLocationPickerMarker
import hu.mostoha.mobile.android.huki.extensions.addLongClickHandlerOverlay
import hu.mostoha.mobile.android.huki.extensions.addMapMovedListener
import hu.mostoha.mobile.android.huki.extensions.addOktBasePolyline
import hu.mostoha.mobile.android.huki.extensions.addOktRoute
import hu.mostoha.mobile.android.huki.extensions.addOverlay
import hu.mostoha.mobile.android.huki.extensions.addPlaceCategoryMarker
import hu.mostoha.mobile.android.huki.extensions.addPlaceDetailsMarker
import hu.mostoha.mobile.android.huki.extensions.addPolygon
import hu.mostoha.mobile.android.huki.extensions.addPolyline
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerMarker
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerPolyline
import hu.mostoha.mobile.android.huki.extensions.addScaleBarOverlay
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoomIn
import hu.mostoha.mobile.android.huki.extensions.areInfoWindowsClosed
import hu.mostoha.mobile.android.huki.extensions.center
import hu.mostoha.mobile.android.huki.extensions.clearFocusAndHideKeyboard
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindows
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindowsForMarkerType
import hu.mostoha.mobile.android.huki.extensions.doOnInfoWindows
import hu.mostoha.mobile.android.huki.extensions.gone
import hu.mostoha.mobile.android.huki.extensions.hasNoOverlay
import hu.mostoha.mobile.android.huki.extensions.hasOverlay
import hu.mostoha.mobile.android.huki.extensions.hideAll
import hu.mostoha.mobile.android.huki.extensions.hideOverlay
import hu.mostoha.mobile.android.huki.extensions.isDarkMode
import hu.mostoha.mobile.android.huki.extensions.isGooglePlayServicesAvailable
import hu.mostoha.mobile.android.huki.extensions.isGpxFileIntent
import hu.mostoha.mobile.android.huki.extensions.isLocationPermissionGranted
import hu.mostoha.mobile.android.huki.extensions.locationPermissions
import hu.mostoha.mobile.android.huki.extensions.openInfoWindows
import hu.mostoha.mobile.android.huki.extensions.openUrl
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.extensions.removeMarker
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.removeOverlays
import hu.mostoha.mobile.android.huki.extensions.resetOrientation
import hu.mostoha.mobile.android.huki.extensions.setStatusBarColor
import hu.mostoha.mobile.android.huki.extensions.shouldShowLocationRationale
import hu.mostoha.mobile.android.huki.extensions.showErrorSnackbar
import hu.mostoha.mobile.android.huki.extensions.showOnly
import hu.mostoha.mobile.android.huki.extensions.showOverlay
import hu.mostoha.mobile.android.huki.extensions.showPopupMenu
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.switchOverlayVisibility
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.toggleInfoWindows
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.extensions.withOffset
import hu.mostoha.mobile.android.huki.model.domain.DeeplinkEvent
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.GPX_WAYPOINT
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_MY_LOCATION
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_PICKED_LOCATION
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_SEARCH
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.OKT_WAYPOINT
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.domain.isZero
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.domain.toOsm
import hu.mostoha.mobile.android.huki.model.mapper.PlaceAreaMapper
import hu.mostoha.mobile.android.huki.model.ui.CompassState
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikeModeUiModel
import hu.mostoha.mobile.android.huki.model.ui.HomeEvents
import hu.mostoha.mobile.android.huki.model.ui.InsetResult
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.PermissionResult
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.selectedRoute
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.DistanceInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.GpxMarkerInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceCategoryMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceDetailsMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RotationGestureOverlay
import hu.mostoha.mobile.android.huki.osmdroid.tileprovider.AwsMapTileProviderBasic
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.adapter.PlaceCategoryAdapter.Companion.addSelectionChip
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.gpx.GpxDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.history.HistoryFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.newfeatures.NewFeaturesBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.oktroutes.OktRoutesBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.placecategory.PlaceCategoryBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.placecategory.PlaceCategoryEvent
import hu.mostoha.mobile.android.huki.ui.home.placecategory.PlaceCategoryEventViewModel
import hu.mostoha.mobile.android.huki.ui.home.placecategory.PlaceCategoryFragment
import hu.mostoha.mobile.android.huki.ui.home.placedetails.PlaceDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderPopup
import hu.mostoha.mobile.android.huki.ui.home.placefinder.PlaceFinderViewModel
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.RoutePlannerFragment
import hu.mostoha.mobile.android.huki.ui.home.routeplanner.RoutePlannerViewModel
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.settings.SettingsViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.InsetSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.MapTouchEventSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.MapTouchEvents
import hu.mostoha.mobile.android.huki.ui.home.shared.PermissionSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.PickLocationEventSharedViewModel
import hu.mostoha.mobile.android.huki.ui.home.shared.PickLocationEvents
import hu.mostoha.mobile.android.huki.ui.home.support.ProductsViewModel
import hu.mostoha.mobile.android.huki.ui.home.support.SupportFragment
import hu.mostoha.mobile.android.huki.util.DARK_MODE_HIKING_LAYER_BRIGHTNESS
import hu.mostoha.mobile.android.huki.util.HIKE_MODE_INFO_WINDOW_SHOW_DELAY
import hu.mostoha.mobile.android.huki.util.HUNGARY_BOUNDING_BOX
import hu.mostoha.mobile.android.huki.util.KEKTURA_URL
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.MAP_MAX_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.OKT_OVERLAY_ID
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.util.ROUTE_PLANNER_MAX_WAYPOINT_COUNT
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_FOLLOW_LOCATION
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_HIKE_MODE
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_MY_LOCATION
import hu.mostoha.mobile.android.huki.util.adjustBrightness
import hu.mostoha.mobile.android.huki.util.color
import hu.mostoha.mobile.android.huki.util.colorStateList
import hu.mostoha.mobile.android.huki.util.distanceBetween
import hu.mostoha.mobile.android.huki.util.getBrightnessColorMatrix
import hu.mostoha.mobile.android.huki.util.getColorScaledMatrix
import hu.mostoha.mobile.android.huki.util.productIconColor
import hu.mostoha.mobile.android.huki.views.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import timber.log.Timber
import javax.inject.Inject

@Suppress("LargeClass")
@AndroidEntryPoint
class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    @Inject
    lateinit var myLocationProvider: AsyncMyLocationProvider

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var deeplinkHandler: DeeplinkHandler

    private val homeViewModel: HomeViewModel by viewModels()
    private val layersViewModel: LayersViewModel by viewModels()
    private val placeFinderViewModel: PlaceFinderViewModel by viewModels()
    private val routePlannerViewModel: RoutePlannerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val insetSharedViewModel: InsetSharedViewModel by viewModels()
    private val permissionSharedViewModel: PermissionSharedViewModel by viewModels()
    private val mapTouchEventSharedViewModel: MapTouchEventSharedViewModel by viewModels()
    private val pickLocationEventViewModel: PickLocationEventSharedViewModel by viewModels()
    private val productsViewModel: ProductsViewModel by viewModels()
    private val placeCategoryEventViewModel: PlaceCategoryEventViewModel by viewModels()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private val homeContainer by lazy { binding.homeContainer }
    private val homeMapView by lazy { binding.homeMapView }
    private val homeHeaderBarrierView by lazy { binding.homeHeaderBarrierView }
    private val homeSearchBarInputLayout by lazy { binding.homeSearchBarInputLayout }
    private val homeSearchBarInput by lazy { binding.homeSearchBarInput }
    private val homeSearchBarPopupAnchor by lazy { binding.homeSearchBarPopupAnchor }
    private val homeRoutePlannerHeaderGroup by lazy { binding.homeRoutePlannerHeaderGroup }
    private val homeHikeModeHeaderGroup by lazy { binding.homeHikeModeHeaderGroup }

    private val homeMyLocationFab by lazy { binding.homeMyLocationFab }
    private val homeRoutePlannerFab by lazy { binding.homeRoutePlannerFab }
    private val homeLayersFab by lazy { binding.homeLayersFab }
    private val homeSupportFab by lazy { binding.homeSupportFab }
    private val homeSettingsFab by lazy { binding.homeSettingsFab }
    private val homeHistoryFab by lazy { binding.homeHistoryFab }
    private val homePlaceCategoriesFab by lazy { binding.homePlaceCategoriesFab }
    private val homeOktFab by lazy { binding.homeOktFab }
    private val placeCategoriesChipGroup by lazy { binding.homePlaceCategoryChipGroup }
    private val homeHikeModeFab by lazy { binding.homeHikeModeFab }
    private val homeCompassFab by lazy { binding.homeCompassFab }
    private val homeAltitudeContainer by lazy { binding.homeAltitudeContainer }
    private val homeAltitudeText by lazy { binding.homeAltitudeText }
    private val mapZoomInFab by lazy { binding.mapZoomInFab }
    private val mapZoomOutFab by lazy { binding.mapZoomOutFab }

    private lateinit var placeFinderPopup: PlaceFinderPopup
    private lateinit var placeDetailsBottomSheet: PlaceDetailsBottomSheetDialog
    private lateinit var placeCategoryBottomSheetDialog: PlaceCategoryBottomSheetDialog
    private lateinit var hikingRoutesBottomSheet: HikingRoutesBottomSheetDialog
    private lateinit var gpxDetailsBottomSheet: GpxDetailsBottomSheetDialog
    private lateinit var oktRoutesBottomSheet: OktRoutesBottomSheetDialog
    private lateinit var bottomSheets: List<BottomSheetDialog>

    private var rotationGestureOverlay: RotationGestureOverlay? = null
    private var myLocationOverlay: MyLocationOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTheme()
        initWindow()
        initViews()
        initPermissions()
        initIntentHandlers(intent)
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.updateMyLocationConfig(isLocationPermissionGranted())

        homeMapView.onResume()
    }

    override fun onPause() {
        homeViewModel.saveMapBoundingBox(homeMapView.boundingBox.toDomain(), homeMapView.zoomLevelDouble)

        myLocationOverlay?.disableMyLocation()

        homeMapView.onPause()

        super.onPause()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        bottomSheets.hideAll()
    }

    private fun initTheme() {
        lifecycleScope.launch {
            val theme = settingsRepository.getTheme().firstOrNull()
            AppCompatDelegate.setDefaultNightMode(
                when (theme) {
                    Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            )
        }
    }

    private fun initWindow() {
        setStatusBarColor(android.R.color.transparent)

        val searchBarTopMargin = homeHeaderBarrierView.marginTop

        ViewCompat.setOnApplyWindowInsetsListener(homeContainer) { _, windowInsetsCompat ->
            val insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())

            homeHeaderBarrierView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(top = searchBarTopMargin + insets.top)
            }

            insetSharedViewModel.updateResult(InsetResult(insets.top))

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initViews() {
        initMapView()
        initSearchBar()
        initPlaceFinderPopup()
        initFabs()
        initBottomSheets()
        initSettingsFlows()
    }

    private fun initMapView() {
        homeMapView.apply {
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            maxZoomLevel = MAP_MAX_ZOOM_LEVEL
            addMapMovedListener {
                homeViewModel.clearHikingRoutes()
                hikingRoutesBottomSheet.hide()
            }
            addOverlay(OsmLicencesOverlay(this@HomeActivity, analyticsService), OverlayComparator)
            if (isDarkMode()) {
                overlayManager.tilesOverlay.setColorFilter(getColorScaledMatrix(getColor(R.color.colorScaleDarkMap)))
            }
            addOnFirstLayoutListener { _, _, _, _, _ ->
                restoreBoundingBox()

                initFlows()

                homeMapView.addScaleBarOverlay()

                rotationGestureOverlay = RotationGestureOverlay(homeMapView)
                    .apply {
                        isEnabled = false
                        mapRotationListener = {
                            homeViewModel.setFreeCompass(homeMapView.mapOrientation)
                        }
                    }
                    .also { homeMapView.addOverlay(it, OverlayComparator) }

                pickLocationEventViewModel.updateEvent(PickLocationEvents.LocationPickEnabled)
            }
            setOnTouchListener { view, _ ->
                view.performClick()

                clearSearchBarInput()
                mapTouchEventSharedViewModel.updateEvent(MapTouchEvents.MAP_TOUCHED)
                false
            }
        }
    }

    private fun restoreBoundingBox() {
        lifecycleScope.launch {
            val boundingBox = homeViewModel.getSavedBoundingBox()
            if (boundingBox == null || boundingBox.isZero()) {
                Timber.d("MapConfig: there wasn't saved bounding box, showing Hungary")
                homeMapView.zoomToBoundingBox(HUNGARY_BOUNDING_BOX.toOsm(), false)
            } else {
                Timber.d("MapConfig: restoring bounding box: $boundingBox")
                homeMapView.zoomToBoundingBox(boundingBox.toOsm(), false)
            }
        }
    }

    private fun initSearchBar() {
        homeSearchBarInputLayout.setEndIconOnClickListener {
            clearSearchBarInput()
        }
        homeSearchBarInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                placeFinderViewModel.initPlaceFinder(PlaceFinderFeature.MAP)
            }
        }
        homeSearchBarInput.addTextChangedListener { editable ->
            val text = editable.toString()
            if (homeSearchBarInput.hasFocus() && text.isNotEmpty()) {
                if (text.length >= PLACE_FINDER_MIN_TRIGGER_LENGTH) {
                    placeFinderViewModel.loadPlaces(text, homeMapView.boundingBox.toDomain(), MAP_SEARCH)
                } else {
                    placeFinderViewModel.initPlaceFinder(PlaceFinderFeature.MAP)
                }
            }
        }
        homeSearchBarInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearSearchBarInput()
                true
            } else {
                false
            }
        }
    }

    private fun initPlaceFinderPopup() {
        placeFinderPopup = PlaceFinderPopup(
            context = this,
            onPlaceClick = { placeUiModel ->
                analyticsService.placeFinderPlaceClicked(
                    searchText = homeSearchBarInput.text.toString(),
                    placeName = placeUiModel.primaryText.resolve(this@HomeActivity),
                    isFromHistory = placeUiModel.historyInfo != null
                )

                clearSearchBarInput()

                homeViewModel.loadPlaceDetails(placeUiModel)
            },
            onMyLocationClick = {
                clearSearchBarInput()

                when {
                    !isGooglePlayServicesAvailable() -> {
                        showGooglePlayServiceNotAvailableDialog()
                    }
                    isLocationPermissionGranted() -> {
                        lifecycleScope.launch {
                            val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()
                                ?.toLocation()
                                ?.toGeoPoint()

                            if (lastKnownLocation == null) {
                                showErrorSnackbar(
                                    homeContainer,
                                    R.string.place_finder_my_location_error_null_location.toMessage(),
                                )
                            } else {
                                homeViewModel.loadPlaceDetailsWithGeocoding(lastKnownLocation, MAP_MY_LOCATION)
                            }
                        }
                    }
                    shouldShowLocationRationale() -> {
                        showLocationRationaleDialog()
                    }
                    else -> {
                        permissionLauncher.launch(locationPermissions)
                    }
                }
            },
            onPickLocationClick = {
                clearSearchBarInput()

                showSnackbar(
                    homeContainer,
                    R.string.place_finder_pick_location_message.toMessage(),
                    R.drawable.ic_snackbar_place_finder_pick_location
                )
            },
            onShowMoreHistoryClick = {
                clearSearchBarInput()

                supportFragmentManager.addFragment(R.id.homeFragmentContainer, HistoryFragment::class.java)
            }
        )
    }

    private fun clearSearchBarInput() {
        homeSearchBarInput.text?.clear()
        homeSearchBarInput.clearFocusAndHideKeyboard()
        placeFinderViewModel.cancelSearch()
    }

    private fun initFabs() {
        homeMyLocationFab.setOnClickListener {
            analyticsService.myLocationClicked()

            when {
                !isGooglePlayServicesAvailable() -> {
                    showGooglePlayServiceNotAvailableDialog()
                }
                isLocationPermissionGranted() -> {
                    homeViewModel.updateMyLocationConfig(
                        isLocationPermissionEnabled = true,
                        isFollowLocationEnabled = true,
                    )
                }
                shouldShowLocationRationale() -> {
                    showLocationRationaleDialog()
                }
                else -> {
                    permissionLauncher.launch(locationPermissions)
                }
            }
        }
        homeRoutePlannerFab.setOnClickListener {
            analyticsService.routePlannerClicked()

            bottomSheets.hideAll()
            homeViewModel.clearAllOverlay()

            routePlannerViewModel.initWaypoints()

            RoutePlannerFragment.addFragment(
                supportFragmentManager,
                R.id.homeRoutePlannerContainer,
                homeMapView.boundingBox.toDomain()
            )
        }
        homeLayersFab.setOnClickListener {
            analyticsService.layersClicked()
            homeViewModel.clearFollowLocation()
            LayersBottomSheetDialogFragment().show(supportFragmentManager, LayersBottomSheetDialogFragment.TAG)
        }
        homeSupportFab.setOnClickListener {
            analyticsService.supportClicked()
            supportFragmentManager.addFragment(R.id.homeFragmentContainer, SupportFragment::class.java)
        }
        homeSettingsFab.setOnClickListener {
            analyticsService.settingsClicked()
            SettingsBottomSheetDialogFragment().show(supportFragmentManager, SettingsBottomSheetDialogFragment.TAG)
        }
        homeHistoryFab.setOnClickListener {
            analyticsService.historyClicked()
            homeViewModel.clearFollowLocation()
            supportFragmentManager.addFragment(R.id.homeFragmentContainer, HistoryFragment::class.java)
        }
        homePlaceCategoriesFab.setOnClickListener {
            analyticsService.placeCategoryFabClicked()
            PlaceCategoryFragment.addFragment(
                supportFragmentManager,
                R.id.homeFragmentContainer,
                homeMapView.boundingBox.toDomain()
            )
        }
        homeOktFab.setOnClickListener {
            showOktPopupMenu()
        }
        homeHikeModeFab.setOnClickListener {
            analyticsService.hikeModeClicked()
            homeViewModel.toggleHikeMode()
        }
        homeCompassFab.setOnClickListener {
            analyticsService.liveCompassClicked()
            homeViewModel.toggleLiveCompass(homeMapView.mapOrientation)
        }
        mapZoomInFab.setOnClickListener {
            homeMapView.controller.zoomIn()
            myLocationOverlay?.lockZoom(homeMapView.zoomLevelDouble.inc())
        }
        mapZoomOutFab.setOnClickListener {
            homeMapView.controller.zoomOut()
            myLocationOverlay?.lockZoom(homeMapView.zoomLevelDouble.dec())
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

    private fun showGooglePlayServiceNotAvailableDialog() {
        MaterialAlertDialogBuilder(this@HomeActivity, R.style.DefaultMaterialDialog)
            .setTitle(R.string.gps_not_available_title)
            .setMessage(R.string.gps_not_available_message)
            .setPositiveButton(R.string.gps_not_available_positive_button, null)
            .show()
    }

    private fun showOktPopupMenu() {
        showPopupMenu(
            anchorView = homeOktFab,
            actionItems = listOf(
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_okt_title,
                        subTitleId = R.string.okt_okt_subtitle,
                        iconId = R.drawable.ic_okt_okt
                    ),
                    onClick = {
                        val oktType = OktType.OKT
                        analyticsService.oktClicked(oktType)
                        homeViewModel.loadOktRoutes(oktType)
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_rpddk_title,
                        subTitleId = R.string.okt_rpddk_subtitle,
                        iconId = R.drawable.ic_okt_rpddk
                    ),
                    onClick = {
                        val oktType = OktType.RPDDK
                        analyticsService.oktClicked(oktType)
                        homeViewModel.loadOktRoutes(oktType)
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = R.string.okt_akt_title,
                        subTitleId = R.string.okt_akt_subtitle,
                        iconId = R.drawable.ic_okt_akt
                    ),
                    onClick = {
                        val oktType = OktType.AKT
                        analyticsService.oktClicked(oktType)
                        homeViewModel.loadOktRoutes(oktType)
                    }
                ),
                PopupMenuActionItem(
                    popupMenuItem = PopupMenuItem(
                        titleId = null,
                        subTitleId = R.string.okt_official_website_subtitle,
                        iconId = R.drawable.ic_okt_info
                    ),
                    onClick = {
                        openUrl(KEKTURA_URL)
                    }
                )
            ),
            width = R.dimen.okt_popup_menu_width,
        )
    }

    private fun initBottomSheets() {
        hikingRoutesBottomSheet = HikingRoutesBottomSheetDialog(binding.homeHikingRoutesBottomSheetContainer)
        gpxDetailsBottomSheet = GpxDetailsBottomSheetDialog(
            binding.homeGpxDetailsBottomSheetContainer,
            analyticsService
        )
        placeDetailsBottomSheet = PlaceDetailsBottomSheetDialog(
            binding.homePlaceDetailsBottomSheetContainer,
            analyticsService
        )
        placeCategoryBottomSheetDialog = PlaceCategoryBottomSheetDialog(
            binding.homePlaceCategoryBottomSheetContainer,
            analyticsService
        )
        oktRoutesBottomSheet = OktRoutesBottomSheetDialog(
            binding.homeOktRoutesBottomSheetContainer,
            analyticsService
        )
        bottomSheets = listOf(
            placeDetailsBottomSheet,
            hikingRoutesBottomSheet,
            gpxDetailsBottomSheet,
            placeCategoryBottomSheetDialog,
            oktRoutesBottomSheet,
        )

        placeDetailsBottomSheet.hide()
        hikingRoutesBottomSheet.hide()
        gpxDetailsBottomSheet.hide()
        placeCategoryBottomSheetDialog.hide()
        oktRoutesBottomSheet.hide()
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
                    )
                }
            }
        }
    }

    private fun enableMyLocationMonitoring() {
        Timber.d("MyLocation: my location monitoring enabled")

        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationOverlay(lifecycleScope, myLocationProvider, homeMapView)
                .apply {
                    runOnFirstFix {
                        postMain {
                            if (isFollowLocationEnabled) {
                                homeMyLocationFab.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                            } else {
                                homeMyLocationFab.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)
                            }
                        }
                    }
                    onFollowLocationFirstFix = {
                        homeMyLocationFab.setImageResource(R.drawable.ic_home_fab_my_location_fixed)
                    }
                    onFollowLocationDisabled = {
                        homeMyLocationFab.setImageResource(R.drawable.ic_home_fab_my_location_not_fixed)

                        homeViewModel.clearFollowLocation()
                        homeViewModel.setFreeCompass(homeMapView.mapOrientation)
                    }
                    onOrientationChanged = { rotation ->
                        homeCompassFab.rotation = rotation
                    }
                    homeMapView.addOverlay(this, OverlayComparator)
                }
        }
        myLocationOverlay?.let { overlay ->
            lifecycleScope.launch {
                overlay.startLocationFlow()
                    .distinctUntilChanged()
                    .onEach { myLocation ->
                        initAltitude(myLocation.altitude)

                        homeMapView.doOnInfoWindows<DistanceInfoWindow> { marker, infoWindow ->
                            val markerLocation = marker.position.toLocation()

                            infoWindow.title = DistanceFormatter
                                .formatWithoutScale(myLocation.toLocation().distanceBetween(markerLocation))
                                .resolve(this@HomeActivity)
                        }
                    }
                    .collect()
            }
        }
    }

    private fun enableFollowingLocation(isEnabled: Boolean) {
        Timber.d("Follow location is enabled: $isEnabled")

        val locationOverlay = myLocationOverlay ?: return
        val previouslyEnabled = locationOverlay.isFollowLocationEnabled

        when {
            !previouslyEnabled && isEnabled -> {
                homeMyLocationFab.setImageResource(R.drawable.ic_anim_home_fab_my_location_not_fixed)
                homeMyLocationFab.startDrawableAnimation()

                locationOverlay.enableFollowLocation()
            }
            !isEnabled -> {
                locationOverlay.disableFollowLocation()
            }
        }
    }

    private fun initAltitude(altitude: Double) {
        if (altitude >= 1.0) {
            homeAltitudeText.text = getString(R.string.default_distance_template_m, altitude.toInt().toString())
            homeAltitudeContainer.visible()
        } else {
            homeAltitudeText.text = null
            homeAltitudeContainer.gone()
        }
    }

    private fun initFlows() {
        initMapFlows()
        initHomeFlows()
        initLayersFlows()
        initPlaceFinderFlows()
        initRoutePlannerFlows()
        initProductFlows()
        initPlaceCategoryFlows()
    }

    private fun initPlaceCategoryFlows() {
        lifecycleScope.launch {
            homeViewModel.placesByCategories
                .flowWithLifecycle(lifecycle)
                .collect { placesByCategories ->
                    bottomSheets.hideAll()
                    initPlaceCategories(placesByCategories)
                    binding.homePlaceCategoryRefreshButton.setOnClickListener {
                        val placeCategories = placesByCategories.keys
                        val boundingBox = homeMapView.boundingBox.toDomain()

                        homeViewModel.loadPlaceCategories(placeCategories, boundingBox, true)
                    }
                }
        }
        lifecycleScope.launch {
            placeCategoryEventViewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    when (event) {
                        is PlaceCategoryEvent.PlaceCategorySelected -> {
                            val boundingBox = homeMapView.boundingBox.toDomain()

                            homeViewModel.loadPlaceCategories(setOf(event.placeCategory), boundingBox)
                        }
                        is PlaceCategoryEvent.LandscapeSelected -> {
                            homeViewModel.loadLandscapeDetails(event.landscape)
                        }
                        is PlaceCategoryEvent.HikingRouteSelected -> {
                            homeViewModel.loadHikingRoutes(event.placeArea)
                            placeCategoryBottomSheetDialog.hide()
                        }
                    }
                }
        }
    }

    private fun initMapFlows() {
        lifecycleScope.launch {
            homeViewModel.myLocationConfigUiModel
                .map { it.isLocationPermissionEnabled }
                .distinctUntilChanged()
                .onStart { delay(TURN_ON_DELAY_MY_LOCATION) }
                .flowWithLifecycle(lifecycle)
                .collect { isEnabled ->
                    if (isLocationPermissionGranted()) {
                        if (isEnabled) {
                            val isFollowingEnabled = homeViewModel.myLocationConfigUiModel.value.isFollowLocationEnabled

                            enableMyLocationMonitoring()
                            enableFollowingLocation(isFollowingEnabled)
                        }
                    } else {
                        homeViewModel.updateMyLocationConfig(false)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationConfigUiModel
                .map { it.isFollowLocationEnabled }
                .distinctUntilChanged()
                .onStart { delay(TURN_ON_DELAY_FOLLOW_LOCATION) }
                .flowWithLifecycle(lifecycle)
                .collect { isEnabled ->
                    enableFollowingLocation(isEnabled)
                }
        }
        lifecycleScope.launch {
            homeViewModel.hikeModeUiModel
                .flowWithLifecycle(lifecycle)
                .onStart { delay(TURN_ON_DELAY_HIKE_MODE) }
                .collect { uiModel ->
                    postMain {
                        initHikeMode(uiModel)
                    }
                }
        }
        lifecycleScope.launch {
            settingsViewModel.mapScaleFactor
                .flowWithLifecycle(lifecycle)
                .collect { mapScaleFactor ->
                    homeMapView.tilesScaleFactor = mapScaleFactor.toFloat()
                    homeMapView.invalidate()
                }
        }
        lifecycleScope.launch {
            settingsViewModel.newFeatures
                .flowWithLifecycle(lifecycle)
                .collect { newFeatures ->
                    if (newFeatures != null) {
                        NewFeaturesBottomSheetDialogFragment.showDialog(this@HomeActivity, newFeatures)
                    }
                }
        }
    }

    private fun initHomeFlows() {
        lifecycleScope.launch {
            homeViewModel.placeDetails
                .flowWithLifecycle(lifecycle)
                .collect { initPlaceDetails(it) }
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
            homeViewModel.oktRoutes
                .flowWithLifecycle(lifecycle)
                .collect { initOktRoutes(it) }
        }
        lifecycleScope.launch {
            homeViewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .collect { errorMessage ->
                    showErrorSnackbar(homeContainer, errorMessage)
                }
        }
        lifecycleScope.launch {
            layersViewModel.errorMessage
                .flowWithLifecycle(lifecycle)
                .filterNotNull()
                .collect { messageRes ->
                    showToast(messageRes)
                    layersViewModel.clearError()
                }
        }
        lifecycleScope.launch {
            homeViewModel.isLoading
                .flowWithLifecycle(lifecycle)
                .collect { isLoading ->
                    binding.homeSearchBarProgress.visibleOrGone(isLoading)
                }
        }
        lifecycleScope.launch {
            homeViewModel.homeEvents
                .flowWithLifecycle(lifecycle)
                .collect { homeEvents ->
                    initHomeEvents(homeEvents)
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
            routePlannerViewModel.waypointItems
                .combine(homeViewModel.hikeModeUiModel) { waypointItems, hikeModeUiModel ->
                    Pair(waypointItems.isNotEmpty(), hikeModeUiModel.isHikeModeEnabled)
                }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { (isRoutePlannerEnabled, isHikeModeEnabled) ->
                    postMain {
                        when {
                            !isHikeModeEnabled && !isRoutePlannerEnabled -> {
                                homeHikeModeHeaderGroup.visible()
                                homeRoutePlannerHeaderGroup.visible()
                                homeHikeModeFab.show()
                                homeRoutePlannerFab.show()
                            }
                            isHikeModeEnabled && !isRoutePlannerEnabled -> {
                                homeHikeModeHeaderGroup.gone()
                                homeHikeModeFab.show()
                                homeRoutePlannerFab.hide()
                                homeViewModel.clearPlaceCategories()
                            }
                            !isHikeModeEnabled -> {
                                homeRoutePlannerHeaderGroup.gone()
                                homeHikeModeFab.hide()
                                homeRoutePlannerFab.hide()
                                homeViewModel.clearPlaceCategories()
                            }
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
                    if (placeFinderItems != null) {
                        placeFinderPopup.initPlaceFinderItems(homeSearchBarPopupAnchor, placeFinderItems)
                    } else {
                        placeFinderPopup.clearPlaceFinderItems()
                    }
                }
        }
    }

    private fun initSettingsFlows() {
        lifecycleScope.launch {
            permissionSharedViewModel.result
                .flowWithLifecycle(lifecycle)
                .collect { result ->
                    if (PermissionResult.LOCATION_PERMISSION_NEEDED == result) {
                        when {
                            !isGooglePlayServicesAvailable() -> showGooglePlayServiceNotAvailableDialog()
                            shouldShowLocationRationale() -> showLocationRationaleDialog()
                            else -> permissionLauncher.launch(locationPermissions)
                        }
                    }
                    permissionSharedViewModel.clearResult()
                }
        }
        lifecycleScope.launch {
            pickLocationEventViewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { events ->
                    initPickLocationEvents(events)
                }
        }
    }

    private fun initProductFlows() {
        lifecycleScope.launch {
            productsViewModel.productsUiModel
                .map { it.purchases.firstOrNull() }
                .distinctUntilChanged()
                .flowWithLifecycle(lifecycle)
                .collect { purchase ->
                    if (purchase != null) {
                        with(binding.homeSearchBarAppIcon) {
                            setProductIcon(purchase.productType)
                            setOnClickListener {
                                analyticsService.supportClicked()
                                supportFragmentManager.addFragment(
                                    R.id.homeFragmentContainer,
                                    SupportFragment::class.java
                                )
                            }
                        }
                        with(binding.homeSupportFab) {
                            imageTintList = if (isDarkMode()) {
                                purchase.productType.productColorRes
                                    .color(this@HomeActivity)
                                    .adjustBrightness(2.0f)
                                    .colorStateList()
                            } else {
                                purchase.productType.productColorRes
                                    .color(this@HomeActivity)
                                    .productIconColor(this@HomeActivity)
                                    .colorStateList()
                            }
                            setImageResource(R.drawable.ic_home_fab_support_purchased)
                        }
                    }
                    binding.homeSupportFab.show()
                }
        }
    }

    private fun initHomeEvents(homeEvents: HomeEvents) {
        when (homeEvents) {
            is HomeEvents.OsmTagsLoaded -> {
                showPopupMenu(
                    anchorView = binding.root,
                    actionItems = emptyList(),
                    width = R.dimen.default_popup_menu_width_with_header,
                    showAtCenter = true,
                    headerTitle = R.string.osm_data_popup_title.toMessage(),
                    footerMessage = R.string.place_details_osm_id_template
                        .toMessage(listOf(homeEvents.osmId))
                        .resolve(this@HomeActivity)
                        .plus(homeEvents.osmTags)
                        .toMessage()
                )
            }
            is HomeEvents.PlaceCategoryEmpty -> {
                showErrorSnackbar(
                    homeContainer,
                    Message.Res(
                        res = R.string.place_category_empty_message,
                        formatArgs = listOf(
                            homeEvents.emptyCategories.joinToString(", ") {
                                getString(
                                    R.string.place_category_empty_message_category_template,
                                    it.title.resolve(this@HomeActivity)
                                )
                            }
                        )
                    )
                )
            }
        }
    }

    private fun initPickLocationEvents(events: PickLocationEvents) {
        when (events) {
            PickLocationEvents.LocationPickEnabled -> {
                homeMapView.addLongClickHandlerOverlay { geoPoint ->
                    homeMapView.addLocationPickerMarker(
                        geoPoint = geoPoint,
                        onSaveClick = {
                            homeViewModel.loadPlaceDetailsWithGeocoding(geoPoint, MAP_PICKED_LOCATION)
                        },
                    )
                }
            }
            PickLocationEvents.LocationPickDisabled -> {
                homeMapView.removeOverlay(OverlayType.MAP_TOUCH_EVENTS)
            }
            PickLocationEvents.RoutePlannerPickStarted -> {
                homeMapView.addLongClickHandlerOverlay { geoPoint ->
                    homeMapView.addLocationPickerMarker(
                        geoPoint = geoPoint,
                        onSaveClick = {
                            pickLocationEventViewModel.updateEvent(
                                PickLocationEvents.RoutePlannerPickEnded(geoPoint)
                            )
                        },
                        onCloseClick = {
                            pickLocationEventViewModel.updateEvent(
                                PickLocationEvents.LocationPickDisabled
                            )
                        }
                    )
                }
            }
            is PickLocationEvents.RoutePlannerPickEnded -> {
                homeMapView.removeOverlay(OverlayType.MAP_TOUCH_EVENTS)
            }
        }
    }

    private fun initIntentHandlers(intent: Intent?) {
        if (intent != null) {
            if (intent.isGpxFileIntent()) {
                layersViewModel.loadGpx(intent.data)
                homeViewModel.clearFollowLocation()

                analyticsService.gpxImportedByIntent()
            }

            when (val event = deeplinkHandler.handleDeeplink(intent)) {
                is DeeplinkEvent.LandscapeDetails -> {
                    homeViewModel.loadLandscapeDetails(event.osmId)
                }
                is DeeplinkEvent.PlaceDetails -> {
                    homeViewModel.loadPlaceDetailsWithGeocoding(GeoPoint(event.lat, event.lon), MAP_SEARCH)
                }
                null -> Unit
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

    private fun initHikeMode(uiModel: HikeModeUiModel) {
        Timber.d("HikeModeUiModel updated: $uiModel")

        if (uiModel.isHikeModeEnabled) {
            rotationGestureOverlay?.isEnabled = true
            myLocationOverlay?.isLiveCompassEnabled = uiModel.compassState == CompassState.Live

            homeHikeModeFab.backgroundTintList = getColorStateList(R.color.colorPrimary)
            homeHikeModeFab.imageTintList = getColorStateList(R.color.colorOnPrimary)

            when (uiModel.compassState) {
                is CompassState.North -> {
                    homeCompassFab.backgroundTintList = getColorStateList(R.color.colorBackground)
                    homeCompassFab.imageTintList = getColorStateList(R.color.colorPrimaryIconStrong)

                    homeMapView.resetOrientation()
                    homeCompassFab.rotation = 0f
                }
                is CompassState.Live -> {
                    homeCompassFab.backgroundTintList = getColorStateList(R.color.colorPrimary)
                    homeCompassFab.imageTintList = getColorStateList(R.color.colorOnPrimary)
                }
                is CompassState.Free -> {
                    homeCompassFab.backgroundTintList = getColorStateList(R.color.colorBackground)
                    homeCompassFab.imageTintList = getColorStateList(R.color.colorPrimaryIconStrong)

                    homeCompassFab.rotation = -uiModel.compassState.mapOrientation
                }
            }

            homeCompassFab.show()
            mapZoomInFab.show()
            mapZoomOutFab.show()

            if (homeMapView.areInfoWindowsClosed<DistanceInfoWindow>()) {
                postMainDelayed(HIKE_MODE_INFO_WINDOW_SHOW_DELAY) {
                    homeMapView.openInfoWindows<DistanceInfoWindow>()
                }
            }

            bottomSheets.hideAll()
        } else {
            myLocationOverlay?.isLiveCompassEnabled = false
            rotationGestureOverlay?.isEnabled = false

            homeHikeModeFab.backgroundTintList = getColorStateList(R.color.colorBackground)
            homeHikeModeFab.imageTintList = getColorStateList(R.color.colorPrimaryIconStrong)

            homeCompassFab.hide()
            mapZoomInFab.hide()
            mapZoomOutFab.hide()

            homeMapView.closeInfoWindows<DistanceInfoWindow>()
            homeMapView.resetOrientation()
        }
    }

    private fun initLandscapeDetails(landscapeDetails: LandscapeDetailsUiModel?) {
        when {
            landscapeDetails == null -> {
                homeMapView.removeOverlay(OverlayType.LANDSCAPE)
            }
            homeMapView.hasNoOverlay(landscapeDetails.landscapeUiModel.osmId) -> {
                homeMapView.removeOverlay(OverlayType.LANDSCAPE)

                val landscapeUiModel = landscapeDetails.landscapeUiModel
                val geometryUiModel = landscapeDetails.geometryUiModel
                val boundingBox = BoundingBox.fromGeoPoints(geometryUiModel.ways.flatMap { it.geoPoints })
                val offsetBoundingBox = boundingBox.withOffset(homeMapView, OffsetType.LANDSCAPE)
                val overlays = mutableListOf<OverlayWithIW>()
                val placeArea = PlaceAreaMapper.map(landscapeUiModel, boundingBox)

                geometryUiModel.ways.forEach { way ->
                    val landscapeOverlays = homeMapView.addLandscapePolyOverlay(
                        overlayId = landscapeUiModel.osmId,
                        way = way,
                        onClick = {
                            initPlaceCategoryBottomSheet(placeArea)

                            homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                        }
                    )
                    overlays.addAll(landscapeOverlays)
                }

                initPlaceCategoryBottomSheet(placeArea)

                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            }
        }
    }

    private fun initPlaceDetails(placeDetails: PlaceDetailsUiModel?) {
        when {
            placeDetails == null -> {
                homeMapView.closeInfoWindowsForMarkerType<DistanceInfoWindow, PlaceDetailsMarker>()
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)
            }
            homeMapView.hasNoOverlay(placeDetails.placeUiModel.overlayId) -> {
                when (placeDetails.geometryUiModel) {
                    is GeometryUiModel.Node -> initNodeDetails(placeDetails.placeUiModel)
                    is GeometryUiModel.Way -> initWayDetails(placeDetails)
                    is GeometryUiModel.Relation -> initRelationDetails(placeDetails)
                    is GeometryUiModel.HikingRoute -> initHikingRouteDetails(placeDetails)
                }
            }
        }
    }

    private fun initNodeDetails(placeUiModel: PlaceUiModel) {
        lifecycleScope.launch {
            val lastKnownLocation = myLocationProvider.getLastKnownLocationCoroutine()
                ?.toLocation()
                ?.toGeoPoint()
            val isHikeModeEnabled = homeViewModel.hikeModeUiModel.first().isHikeModeEnabled

            val geoPoint = placeUiModel.geoPoint
            val boundingBox = placeUiModel.boundingBox

            val marker = homeMapView.addPlaceDetailsMarker(
                overlayId = placeUiModel.overlayId,
                name = placeUiModel.primaryText,
                geoPoint = geoPoint,
                iconDrawable = R.drawable.ic_marker_poi.toDrawable(this@HomeActivity),
                isHikeModeEnabled = isHikeModeEnabled,
                myLocation = lastKnownLocation,
                onClick = { marker ->
                    initNodeBottomSheet(placeUiModel, marker)

                    if (boundingBox != null) {
                        val offsetBoundingBox = boundingBox
                            .toOsm()
                            .withOffset(homeMapView, OffsetType.BOTTOM_SHEET)
                        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                    } else {
                        homeMapView.animateCenterAndZoomIn(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
                    }
                }
            )

            initNodeBottomSheet(placeUiModel, marker)

            if (boundingBox != null) {
                homeMapView.zoomToBoundingBox(boundingBox.toOsm(), true)
            } else {
                homeMapView.animateCenterAndZoomIn(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
            }
        }
    }

    private fun initWayDetails(placeDetails: PlaceDetailsUiModel) {
        val geometryUiModel = placeDetails.geometryUiModel as? GeometryUiModel.Way ?: return

        val geoPoints = geometryUiModel.geoPoints
        val boundingBox = BoundingBox.fromGeoPoints(geoPoints)
        val offsetBoundingBox = boundingBox.withOffset(homeMapView, OffsetType.BOTTOM_SHEET)
        if (geometryUiModel.isClosed) {
            homeMapView.addPolygon(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = geoPoints,
                onClick = {
                    initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        } else {
            homeMapView.addPolyline(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = geoPoints,
                onClick = {
                    initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)
                    homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                }
            )
        }

        initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
    }

    private fun initRelationDetails(placeDetails: PlaceDetailsUiModel) {
        val relation = placeDetails.geometryUiModel as? GeometryUiModel.Relation ?: return
        val boundingBox = BoundingBox
            .fromGeoPoints(relation.ways.flatMap { it.geoPoints })
            .withOffset(homeMapView, OffsetType.BOTTOM_SHEET)

        relation.ways.forEach { way ->
            homeMapView.addPolyline(
                overlayId = placeDetails.placeUiModel.osmId,
                geoPoints = way.geoPoints,
                onClick = {
                    initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)
                    homeMapView.zoomToBoundingBox(boundingBox, true)
                }
            )
        }

        initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)

        homeMapView.zoomToBoundingBox(boundingBox, true)
    }

    private fun initHikingRouteDetails(placeDetails: PlaceDetailsUiModel) {
        val hikingRoute = placeDetails.geometryUiModel as? GeometryUiModel.HikingRoute ?: return
        val boundingBox = BoundingBox
            .fromGeoPoints(hikingRoute.ways.flatMap { it.geoPoints })
            .withOffset(homeMapView, OffsetType.BOTTOM_SHEET)

        homeMapView.addHikingRouteDetails(
            overlayId = placeDetails.placeUiModel.osmId,
            relation = hikingRoute,
            iconRes = placeDetails.placeUiModel.iconRes,
            onClick = {
                initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)
                homeMapView.zoomToBoundingBox(boundingBox, true)
            }
        )

        initPolyPlaceDetailsBottomSheet(placeDetails.placeUiModel)

        homeMapView.zoomToBoundingBox(boundingBox, true)
    }

    private fun initOktRoutes(oktRoutes: OktRoutesUiModel?) {
        if (oktRoutes == null) {
            InfoWindow.closeAllInfoWindowsOn(homeMapView)
            homeMapView.removeOverlays(listOf(OverlayType.OKT_ROUTES, OverlayType.OKT_ROUTES_BASE))
            oktRoutesBottomSheet.hide()
            return
        }

        homeMapView.removeOverlay(OverlayType.OKT_ROUTES)

        if (homeMapView.hasNoOverlay(OKT_OVERLAY_ID)) {
            homeMapView.addOktBasePolyline(
                overlayId = OKT_OVERLAY_ID,
                geoPoints = oktRoutes.mapGeoPoints,
                onClick = {
                    homeViewModel.selectOktRoute(it)
                }
            )
        }

        val selectedRoute = oktRoutes.selectedRoute

        val offsetBoundingBox = BoundingBox
            .fromGeoPoints(selectedRoute.geoPoints)
            .withOffset(homeMapView, OffsetType.OKT_ROUTES)

        homeMapView.addOktRoute(
            overlayId = selectedRoute.oktId,
            oktRouteUiModel = selectedRoute,
            onRouteClick = {
                InfoWindow.closeAllInfoWindowsOn(homeMapView)
                initOktRoutesBottomSheet(oktRoutes.oktType, oktRoutes, selectedRoute.oktId)
                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            },
            onWaypointClick = {
                analyticsService.oktWaypointClicked()
            },
            onWaypointNavigationClick = { geoPoint ->
                homeViewModel.loadPlaceDetailsWithGeocoding(geoPoint, OKT_WAYPOINT)
            }
        )

        initOktRoutesBottomSheet(oktRoutes.oktType, oktRoutes, selectedRoute.oktId)

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
    }

    private fun initPlaceCategories(placesByCategories: Map<PlaceCategory, List<PlaceUiModel>>) {
        if (placesByCategories.isEmpty()) {
            binding.homePlaceCategoryContainer.gone()
            placeCategoriesChipGroup.removeAllViews()
            homeMapView.removeOverlays(PlaceCategoryMarker::class.java)
        } else {
            binding.homePlaceCategoryContainer.visible()

            placeCategoriesChipGroup.children.forEach { chip ->
                if (chip.tag !in placesByCategories.keys.map { it.name }) {
                    placeCategoriesChipGroup.removeView(chip)
                }
            }

            placesByCategories.forEach { placeByCategory ->
                val placeCategory = placeByCategory.key
                val places = placeByCategory.value

                if (placeCategoriesChipGroup.children.none { it.tag == placeCategory.name }) {
                    placeCategoriesChipGroup.addSelectionChip(
                        viewTag = placeCategory.name,
                        title = placeCategory.title,
                        backgroundColorRes = placeCategory.categoryColorRes,
                        onClick = {
                            homeViewModel.clearPlaceCategory(placeCategory)
                            removePlaceCategoryMarkers(listOf(placeCategory))
                            homeMapView.invalidate()
                        }
                    )
                }

                places.forEach { placeUiModel ->
                    homeMapView.addPlaceCategoryMarker(
                        placeCategory = placeCategory,
                        geoPoint = placeUiModel.geoPoint,
                        infoWindowTitle = placeUiModel.primaryText.resolve(this@HomeActivity),
                        iconDrawable = placeUiModel.iconRes.toDrawable(this@HomeActivity),
                        onMarkerClick = { marker ->
                            initNodeBottomSheet(placeUiModel, marker)

                            homeMapView.center(placeUiModel.geoPoint)
                        }
                    )
                }
            }
        }
    }

    private fun removePlaceCategoryMarkers(placeCategories: List<PlaceCategory>) {
        placeCategories.forEach { placeCategory ->
            homeMapView.overlays.removeAll {
                it is PlaceCategoryMarker && it.placeCategory == placeCategory
            }
            homeMapView.invalidate()
        }
    }

    private fun initOktRoutesBottomSheet(oktType: OktType, oktRoutes: OktRoutesUiModel, selectedId: String) {
        oktRoutesBottomSheet.init(
            oktType = oktType,
            oktRoutes = oktRoutes.routes,
            selectedOktId = selectedId,
            onRouteClick = { oktId ->
                InfoWindow.closeAllInfoWindowsOn(homeMapView)
                homeViewModel.selectOktRoute(oktId)
            },
            onEdgePointClick = { geoPoint ->
                homeViewModel.loadPlaceDetailsWithGeocoding(geoPoint, OKT_WAYPOINT)
            },
            onCloseClick = {
                homeViewModel.clearOktRoutes()
            }
        )
        bottomSheets.showOnly(oktRoutesBottomSheet)
    }

    private fun initHikingRoutesBottomSheet(hikingRoutes: List<HikingRoutesItem>) {
        hikingRoutesBottomSheet.initBottomSheet(
            hikingRoutes = hikingRoutes,
            onHikingRouteClick = { hikingRoute ->
                homeViewModel.loadHikingRouteDetails(hikingRoute)
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)
                analyticsService.loadHikingRouteDetailsClicked(hikingRoute.name)
            },
            onCloseClick = {
                homeViewModel.clearHikingRoutes()
            }
        )
        bottomSheets.showOnly(hikingRoutesBottomSheet)
    }

    private fun initNodeBottomSheet(placeUiModel: PlaceUiModel, marker: Marker) {
        val routePlanMarkers = homeMapView.overlays
            .filterIsInstance<PlaceDetailsMarker>()
            .map { it.name to it.position }
            .takeLast(ROUTE_PLANNER_MAX_WAYPOINT_COUNT)

        placeDetailsBottomSheet.initNodeBottomSheet(
            placeUiModel = placeUiModel,
            routePlanMarkerCount = routePlanMarkers.size,
            onShowAllPointsClick = {
                homeMapView.closeInfoWindowsForMarkerType<DistanceInfoWindow, PlaceDetailsMarker>()
                placeDetailsBottomSheet.hide()
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)

                homeViewModel.loadPlaceDetailsWithGeometry(placeUiModel)
            },
            onRoutePlanButtonClick = {
                homeViewModel.disableHikeMode()

                placeDetailsBottomSheet.hide()

                if (routePlanMarkers.size <= 1) {
                    routePlannerViewModel.initWaypoint(placeUiModel)
                } else {
                    routePlannerViewModel.initWaypoints(routePlanMarkers)
                }

                homeMapView.removeMarker(marker)
                homeViewModel.clearPlaceDetails()

                RoutePlannerFragment.addFragment(
                    supportFragmentManager,
                    R.id.homeRoutePlannerContainer,
                    homeMapView.boundingBox.toDomain()
                )
            },
            onCloseButtonClick = {
                homeMapView.closeInfoWindowsForMarkerType<DistanceInfoWindow, PlaceDetailsMarker>()
                homeMapView.removeMarker(marker)
                placeDetailsBottomSheet.hide()

                homeViewModel.clearPlaceDetails()
            },
            onPlaceCategoryFinderClick = {
                initPlaceCategoryBottomSheet(PlaceAreaMapper.map(placeUiModel, homeMapView.boundingBox))
            },
            onAllOsmDataClick = {
                homeViewModel.loadOsmTags(placeUiModel)
            }
        )
        bottomSheets.showOnly(placeDetailsBottomSheet)
    }

    private fun initPolyPlaceDetailsBottomSheet(place: PlaceUiModel) {
        placeDetailsBottomSheet.initPolyDetailsBottomSheet(
            placeUiModel = place,
            onCloseButtonClick = {
                placeDetailsBottomSheet.hide()

                homeViewModel.clearPlaceDetails()
            }
        )
        bottomSheets.showOnly(placeDetailsBottomSheet)
    }

    private fun initPlaceCategoryBottomSheet(placeArea: PlaceArea) {
        lifecycleScope.launch {
            postMain {
                placeCategoryBottomSheetDialog.init(
                    placeArea = placeArea,
                    onHikingTrailsClick = {
                        homeViewModel.loadHikingRoutes(placeArea)
                        placeCategoryBottomSheetDialog.hide()
                    },
                    onCategoryClick = { placeCategory ->
                        homeViewModel.loadPlaceCategories(setOf(placeCategory), placeArea.boundingBox)
                    },
                    onCloseClick = {
                        homeViewModel.clearLandscapeDetails()
                        placeCategoryBottomSheetDialog.hide()
                    },
                )
                bottomSheets.showOnly(placeCategoryBottomSheetDialog)
            }
        }
    }

    private fun initGpxDetails(gpxDetailsUiModel: GpxDetailsUiModel?) {
        when {
            gpxDetailsUiModel == null -> {
                InfoWindow.closeAllInfoWindowsOn(homeMapView)
                homeMapView.removeOverlay(OverlayType.GPX)
                return
            }
            homeMapView.hasOverlay(gpxDetailsUiModel.id) -> {
                if (gpxDetailsUiModel.isVisible) {
                    homeMapView.showOverlay(gpxDetailsUiModel.id)
                } else {
                    homeMapView.hideOverlay(gpxDetailsUiModel.id)
                }
            }
            homeMapView.hasNoOverlay(gpxDetailsUiModel.id) -> {
                homeMapView.removeOverlay(OverlayType.GPX)

                val offsetBoundingBox = gpxDetailsUiModel.boundingBox.withOffset(homeMapView, OffsetType.BOTTOM_SHEET)

                homeMapView.addGpxPolyline(
                    overlayId = gpxDetailsUiModel.id,
                    geoPoints = gpxDetailsUiModel.geoPoints,
                    useAltitudeColors = gpxDetailsUiModel.altitudeUiModel != null,
                    onClick = {
                        gpxDetailsBottomSheet.show()
                        homeViewModel.clearFollowLocation()
                        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                    }
                )
                gpxDetailsUiModel.waypoints.forEach { waypointItem ->
                    homeMapView.addGpxMarker(
                        overlayId = gpxDetailsUiModel.id,
                        geoPoint = waypointItem.geoPoint,
                        waypointType = waypointItem.waypointType,
                        infoWindowTitle = waypointItem.name?.resolve(this),
                        infoWindowDescription = waypointItem.description?.resolve(this),
                        onMarkerClick = {
                            gpxDetailsBottomSheet.show()
                            homeViewModel.clearFollowLocation()
                            homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                        },
                        onWaypointClick = {
                            analyticsService.gpxImportClicked()
                        },
                        onWaypointNavigationClick = { geoPoint ->
                            homeViewModel.loadPlaceDetailsWithGeocoding(geoPoint, GPX_WAYPOINT)
                        }
                    )
                }

                postMain {
                    gpxDetailsBottomSheet.initBottomSheet(
                        gpxDetails = gpxDetailsUiModel,
                        onCloseClick = { layersViewModel.clearGpxDetails() },
                        onStartClick = {
                            lifecycleScope.launch {
                                val lastKnownGeoPoint = myLocationProvider.getLastKnownLocationCoroutine()
                                    ?.toLocation()
                                    ?.toGeoPoint()

                                val boundingBox = BoundingBox
                                    .fromGeoPoints(
                                        if (lastKnownGeoPoint != null) {
                                            gpxDetailsUiModel.geoPoints.plus(lastKnownGeoPoint)
                                        } else {
                                            gpxDetailsUiModel.geoPoints
                                        }
                                    )
                                    .withOffset(homeMapView, OffsetType.TOP_SHEET)

                                homeMapView.zoomToBoundingBox(boundingBox, false)

                                homeViewModel.toggleHikeMode()
                            }
                        },
                        onHideClick = {
                            homeMapView.switchOverlayVisibility<GpxPolyline>(gpxDetailsUiModel.id)
                        },
                        onCommentsButtonClick = {
                            homeMapView.toggleInfoWindows<GpxMarkerInfoWindow>()
                        }
                    )
                    bottomSheets.showOnly(gpxDetailsBottomSheet)

                    homeMapView.zoomToBoundingBox(offsetBoundingBox, false)
                }
            }
        }
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
