package hu.mostoha.mobile.android.huki.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.databinding.ActivityHomeBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeLandscapesChipBinding
import hu.mostoha.mobile.android.huki.deeplink.DeeplinkHandler
import hu.mostoha.mobile.android.huki.extensions.OffsetType
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
import hu.mostoha.mobile.android.huki.extensions.addPlaceDetailsMarker
import hu.mostoha.mobile.android.huki.extensions.addPolygon
import hu.mostoha.mobile.android.huki.extensions.addPolyline
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerMarker
import hu.mostoha.mobile.android.huki.extensions.addRoutePlannerPolyline
import hu.mostoha.mobile.android.huki.extensions.addScaleBarOverlay
import hu.mostoha.mobile.android.huki.extensions.animateCenterAndZoom
import hu.mostoha.mobile.android.huki.extensions.areInfoWindowsClosed
import hu.mostoha.mobile.android.huki.extensions.clearFocusAndHideKeyboard
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindows
import hu.mostoha.mobile.android.huki.extensions.closeInfoWindowsForMarkerType
import hu.mostoha.mobile.android.huki.extensions.color
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
import hu.mostoha.mobile.android.huki.extensions.postMain
import hu.mostoha.mobile.android.huki.extensions.postMainDelayed
import hu.mostoha.mobile.android.huki.extensions.removeMarker
import hu.mostoha.mobile.android.huki.extensions.removeOverlay
import hu.mostoha.mobile.android.huki.extensions.removeOverlays
import hu.mostoha.mobile.android.huki.extensions.resetOrientation
import hu.mostoha.mobile.android.huki.extensions.setStatusBarColor
import hu.mostoha.mobile.android.huki.extensions.setTextOrInvisible
import hu.mostoha.mobile.android.huki.extensions.shouldShowLocationRationale
import hu.mostoha.mobile.android.huki.extensions.showErrorSnackbar
import hu.mostoha.mobile.android.huki.extensions.showOnly
import hu.mostoha.mobile.android.huki.extensions.showOverlay
import hu.mostoha.mobile.android.huki.extensions.showSnackbar
import hu.mostoha.mobile.android.huki.extensions.showToast
import hu.mostoha.mobile.android.huki.extensions.startDrawableAnimation
import hu.mostoha.mobile.android.huki.extensions.switchOverlayVisibility
import hu.mostoha.mobile.android.huki.extensions.toDrawable
import hu.mostoha.mobile.android.huki.extensions.visible
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone
import hu.mostoha.mobile.android.huki.extensions.withOffset
import hu.mostoha.mobile.android.huki.extensions.zoomToBoundingBoxPostMain
import hu.mostoha.mobile.android.huki.model.domain.DeeplinkEvent
import hu.mostoha.mobile.android.huki.model.domain.HikingLayer
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.GPX_WAYPOINT
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_MY_LOCATION
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_PICKED_LOCATION
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.MAP_SEARCH
import hu.mostoha.mobile.android.huki.model.domain.PlaceFeature.OKT_WAYPOINT
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toGeoPoint
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.domain.toOsm
import hu.mostoha.mobile.android.huki.model.mapper.HikeRecommenderMapper
import hu.mostoha.mobile.android.huki.model.ui.CompassState
import hu.mostoha.mobile.android.huki.model.ui.GeometryUiModel
import hu.mostoha.mobile.android.huki.model.ui.GpxDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikeModeUiModel
import hu.mostoha.mobile.android.huki.model.ui.HikeRecommendation
import hu.mostoha.mobile.android.huki.model.ui.InsetResult
import hu.mostoha.mobile.android.huki.model.ui.LandscapeDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.OktRoutesUiModel
import hu.mostoha.mobile.android.huki.model.ui.PermissionResult
import hu.mostoha.mobile.android.huki.model.ui.PlaceDetailsUiModel
import hu.mostoha.mobile.android.huki.model.ui.PlaceFinderFeature
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.model.ui.selectedRoute
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import hu.mostoha.mobile.android.huki.osmdroid.OsmLicencesOverlay
import hu.mostoha.mobile.android.huki.osmdroid.infowindow.DistanceInfoWindow
import hu.mostoha.mobile.android.huki.osmdroid.location.AsyncMyLocationProvider
import hu.mostoha.mobile.android.huki.osmdroid.location.MyLocationOverlay
import hu.mostoha.mobile.android.huki.osmdroid.overlay.GpxPolyline
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayComparator
import hu.mostoha.mobile.android.huki.osmdroid.overlay.OverlayType
import hu.mostoha.mobile.android.huki.osmdroid.overlay.PlaceDetailsMarker
import hu.mostoha.mobile.android.huki.osmdroid.overlay.RotationGestureOverlay
import hu.mostoha.mobile.android.huki.osmdroid.tileprovider.AwsMapTileProviderBasic
import hu.mostoha.mobile.android.huki.repository.SettingsRepository
import hu.mostoha.mobile.android.huki.service.AnalyticsService
import hu.mostoha.mobile.android.huki.ui.formatter.DistanceFormatter
import hu.mostoha.mobile.android.huki.ui.home.gpx.GpxDetailsBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikerecommender.HikeRecommenderBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesBottomSheetDialog
import hu.mostoha.mobile.android.huki.ui.home.hikingroutes.HikingRoutesItem
import hu.mostoha.mobile.android.huki.ui.home.history.HistoryFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.layers.LayersViewModel
import hu.mostoha.mobile.android.huki.ui.home.newfeatures.NewFeaturesBottomSheetDialogFragment
import hu.mostoha.mobile.android.huki.ui.home.oktroutes.OktRoutesBottomSheetDialog
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
import hu.mostoha.mobile.android.huki.util.MAP_DEFAULT_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.MAP_MAX_ZOOM_LEVEL
import hu.mostoha.mobile.android.huki.util.PLACE_FINDER_MIN_TRIGGER_LENGTH
import hu.mostoha.mobile.android.huki.util.ROUTE_PLANNER_MAX_WAYPOINT_COUNT
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_FOLLOW_LOCATION
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_HIKE_MODE
import hu.mostoha.mobile.android.huki.util.TURN_ON_DELAY_MY_LOCATION
import hu.mostoha.mobile.android.huki.util.adjustBrightness
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
    private val homeHikeModeFab by lazy { binding.homeHikeModeFab }
    private val homeCompassFab by lazy { binding.homeCompassFab }
    private val homeAltitudeText by lazy { binding.homeAltitudeText }
    private val mapZoomControllerPlus by lazy { binding.mapZoomControllerPlus }
    private val mapZoomControllerMinus by lazy { binding.mapZoomControllerMinus }

    private lateinit var placeFinderPopup: PlaceFinderPopup
    private lateinit var placeDetailsBottomSheet: PlaceDetailsBottomSheetDialog
    private lateinit var hikeRecommenderBottomSheet: HikeRecommenderBottomSheetDialog
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
        homeViewModel.saveMapBoundingBox(homeMapView.boundingBox.toDomain())

        myLocationOverlay?.disableMyLocation()

        homeMapView.onPause()

        super.onPause()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        bottomSheets.hideAll()
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
                    placeFinderViewModel.loadPlaces(text, MAP_SEARCH)
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

            supportFragmentManager.addFragment(R.id.homeRoutePlannerContainer, RoutePlannerFragment::class.java)
        }
        homeLayersFab.setOnClickListener {
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
            analyticsService.gpxHistoryClicked()
            homeViewModel.clearFollowLocation()
            supportFragmentManager.addFragment(R.id.homeFragmentContainer, HistoryFragment::class.java)
        }
        homeHikeModeFab.setOnClickListener {
            analyticsService.hikeModeClicked()
            homeViewModel.toggleHikeMode()
        }
        homeCompassFab.setOnClickListener {
            analyticsService.liveCompassClicked()

            homeViewModel.toggleLiveCompass(homeMapView.mapOrientation)
        }
        mapZoomControllerPlus.setOnClickListener {
            homeMapView.controller.zoomIn()
            myLocationOverlay?.lockZoom(homeMapView.zoomLevelDouble.inc())
        }
        mapZoomControllerMinus.setOnClickListener {
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
        hikeRecommenderBottomSheet = HikeRecommenderBottomSheetDialog(
            binding.homeHikeRecommenderBottomSheetContainer,
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
            hikeRecommenderBottomSheet,
            oktRoutesBottomSheet,
        )

        placeDetailsBottomSheet.hide()
        hikingRoutesBottomSheet.hide()
        gpxDetailsBottomSheet.hide()
        hikeRecommenderBottomSheet.hide()
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
                        homeViewModel.loadLandscapes(myLocation)

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
        val altitudeText = if (altitude >= 1.0) {
            getString(R.string.default_distance_template_m, altitude.toInt().toString())
        } else {
            null
        }
        homeAltitudeText.setTextOrInvisible(altitudeText)
    }

    private fun initFlows() {
        initHomeFlows()
        initMapFlows()
        initLayersFlows()
        initPlaceFinderFlows()
        initRoutePlannerFlows()
        initProductFlows()
    }

    private fun initMapFlows() {
        lifecycleScope.launch {
            homeViewModel.mapConfigUiModel
                .flowWithLifecycle(lifecycle)
                .collect { mapUiModel ->
                    Timber.d("Restoring map config: $mapUiModel")

                    val boundingBox = mapUiModel.boundingBox.toOsm()

                    if (homeMapView.mapOrientation == 0f) {
                        homeMapView.zoomToBoundingBoxPostMain(boundingBox, false)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationUiModel
                .map { it.isLocationPermissionEnabled }
                .distinctUntilChanged()
                .onStart { delay(TURN_ON_DELAY_MY_LOCATION) }
                .flowWithLifecycle(lifecycle)
                .collect { isEnabled ->
                    if (isLocationPermissionGranted()) {
                        if (isEnabled) {
                            val isFollowingEnabled = homeViewModel.myLocationUiModel.value.isFollowLocationEnabled

                            enableMyLocationMonitoring()
                            enableFollowingLocation(isFollowingEnabled)
                        }
                    } else {
                        homeViewModel.updateMyLocationConfig(false)
                    }
                }
        }
        lifecycleScope.launch {
            homeViewModel.myLocationUiModel
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
                            }
                            !isHikeModeEnabled -> {
                                homeRoutePlannerHeaderGroup.gone()
                                homeHikeModeFab.hide()
                                homeRoutePlannerFab.hide()
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
            settingsViewModel.theme
                .flowWithLifecycle(lifecycle)
                .collect { theme ->
                    AppCompatDelegate.setDefaultNightMode(
                        when (theme) {
                            Theme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            Theme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                            Theme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                        }
                    )
                }
        }
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

    private fun initLandscapes(landscapes: List<LandscapeUiModel>) {
        binding.homeLandscapeChipGroup.removeAllViews()
        addChip(
            label = R.string.okt_routes_chip_label.toMessage(),
            iconRes = R.drawable.ic_okt_routes_chip,
            onClick = {
                analyticsService.oktChipClicked()
                homeViewModel.loadOktRoutes()
                layersViewModel.clearGpxDetails()
            }
        )
        landscapes.forEach { landscape ->
            addChip(
                label = landscape.name,
                iconRes = landscape.iconRes,
                onClick = {
                    analyticsService.loadLandscapeClicked(landscape.name.resolve(this))
                    homeViewModel.loadLandscapeDetails(landscape)
                }
            )
        }
    }

    private fun addChip(label: Message, @DrawableRes iconRes: Int, onClick: () -> Unit) {
        val chipBinding = ItemHomeLandscapesChipBinding.inflate(
            layoutInflater,
            binding.homeLandscapeChipGroup,
            false
        )
        with(chipBinding.landscapesChip) {
            text = label.resolve(this@HomeActivity)
            setChipIconResource(iconRes)
            setOnClickListener { onClick.invoke() }
        }
        binding.homeLandscapeChipGroup.addView(chipBinding.root)
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
            mapZoomControllerPlus.show()
            mapZoomControllerMinus.show()

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
            mapZoomControllerPlus.hide()
            mapZoomControllerMinus.hide()

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
                val hikeRecommendation = HikeRecommenderMapper.map(landscapeUiModel, boundingBox)

                geometryUiModel.ways.forEach { way ->
                    val landscapeOverlays = homeMapView.addLandscapePolyOverlay(
                        overlayId = landscapeUiModel.osmId,
                        center = landscapeUiModel.geoPoint,
                        centerMarkerDrawable = landscapeUiModel.markerRes.toDrawable(this),
                        way = way,
                        onClick = {
                            initHikeRecommenderBottomSheet(hikeRecommendation)

                            homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
                        }
                    )
                    overlays.addAll(landscapeOverlays)
                }

                initHikeRecommenderBottomSheet(hikeRecommendation)

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
            homeMapView.hasNoOverlay(placeDetails.placeUiModel.osmId) -> {
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
                overlayId = placeUiModel.osmId,
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
                        homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
                    }
                }
            )

            initNodeBottomSheet(placeUiModel, marker)

            if (boundingBox != null) {
                homeMapView.zoomToBoundingBox(boundingBox.toOsm(), true)
            } else {
                homeMapView.animateCenterAndZoom(geoPoint, MAP_DEFAULT_ZOOM_LEVEL)
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

        if (homeMapView.hasNoOverlay(OKT_ID_FULL_ROUTE)) {
            homeMapView.addOktBasePolyline(
                overlayId = OKT_ID_FULL_ROUTE,
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
                initOktRoutesBottomSheet(oktRoutes, selectedRoute.oktId)
                homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
            },
            onWaypointClick = {
                analyticsService.oktWaypointClicked()
            },
            onWaypointNavigationClick = { geoPoint ->
                homeViewModel.loadPlaceDetailsWithGeocoding(geoPoint, OKT_WAYPOINT)
            }
        )

        initOktRoutesBottomSheet(oktRoutes, selectedRoute.oktId)

        homeMapView.zoomToBoundingBox(offsetBoundingBox, true)
    }

    private fun initOktRoutesBottomSheet(oktRoutes: OktRoutesUiModel, selectedOktId: String) {
        oktRoutesBottomSheet.init(
            oktRoutes = oktRoutes.routes,
            selectedOktId = selectedOktId,
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
        placeDetailsBottomSheet.initNodeBottomSheet(
            placeUiModel = placeUiModel,
            onShowAllPointsClick = {
                homeMapView.closeInfoWindowsForMarkerType<DistanceInfoWindow, PlaceDetailsMarker>()
                placeDetailsBottomSheet.hide()
                homeMapView.removeOverlay(OverlayType.PLACE_DETAILS)

                homeViewModel.loadPlaceDetailsWithGeometry(placeUiModel)
            },
            onRoutePlanButtonClick = {
                homeViewModel.disableHikeMode()

                placeDetailsBottomSheet.hide()

                val markers = homeMapView.overlays
                    .filterIsInstance<PlaceDetailsMarker>()
                    .map { it.name to it.position }
                    .takeLast(ROUTE_PLANNER_MAX_WAYPOINT_COUNT)

                if (markers.size <= 1) {
                    routePlannerViewModel.initWaypoint(placeUiModel)
                } else {
                    routePlannerViewModel.initWaypoints(markers)
                }

                homeMapView.removeMarker(marker)
                homeViewModel.clearPlaceDetails()

                supportFragmentManager.addFragment(R.id.homeRoutePlannerContainer, RoutePlannerFragment::class.java)
            },
            onCloseButtonClick = {
                homeMapView.closeInfoWindowsForMarkerType<DistanceInfoWindow, PlaceDetailsMarker>()
                homeMapView.removeMarker(marker)
                placeDetailsBottomSheet.hide()

                homeViewModel.clearPlaceDetails()
            },
            onHikeRecommenderClick = {
                val hikeRecommendation = HikeRecommenderMapper.map(placeUiModel, homeMapView.boundingBox)

                initHikeRecommenderBottomSheet(hikeRecommendation)
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

    private fun initHikeRecommenderBottomSheet(hikeRecommendation: HikeRecommendation) {
        lifecycleScope.launch {
            val isInfoButtonEnabled = settingsRepository.isHikeRecommenderInfoEnabled().first()

            postMain {
                hikeRecommenderBottomSheet.init(
                    hikeRecommendation = hikeRecommendation,
                    isInfoButtonEnabled = isInfoButtonEnabled,
                    onHikingTrailsClick = {
                        val title = hikeRecommendation.title.resolve(this@HomeActivity)
                        val boundingBox = hikeRecommendation.hikingRoutesBoundingBox

                        homeViewModel.loadHikingRoutes(title, boundingBox)
                        hikeRecommenderBottomSheet.hide()
                    },
                    onCloseClick = {
                        homeViewModel.clearLandscapeDetails()
                        hikeRecommenderBottomSheet.hide()
                    },
                    onCloseInfoTextClick = {
                        lifecycleScope.launch {
                            settingsRepository.saveHikeRecommenderInfoEnabled(false)
                        }
                    },
                )
                bottomSheets.showOnly(hikeRecommenderBottomSheet)
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
