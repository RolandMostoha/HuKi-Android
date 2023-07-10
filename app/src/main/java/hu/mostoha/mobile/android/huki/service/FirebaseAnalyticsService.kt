package hu.mostoha.mobile.android.huki.service

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.osmdroid.CounterProvider
import org.osmdroid.tileprovider.util.Counters
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor() : AnalyticsService {

    companion object {
        private const val EVENT_SELECT_PLACE = "select_place"
        private const val EVENT_SELECT_LANDSCAPE = "select_landscape"
        private const val EVENT_SELECT_LANDSCAPE_KIRANDULASTIPPEK = "select_landscape_kirandulastippek"
        private const val EVENT_SELECT_LANDSCAPE_KIRANDULASTIPPEK_INFO = "select_landscape_kirandulastippek_info"
        private const val EVENT_SELECT_LANDSCAPE_TERMESZETJARO = "select_landscape_termeszetjaro"
        private const val EVENT_SELECT_LANDSCAPE_TERMESZETJARO_INFO = "select_landscape_termeszetjaro_info"
        private const val EVENT_SELECT_PLACE_DETAILS = "select_place_details"
        private const val EVENT_SEARCH_HIKING_ROUTES = "search_hiking_routes"
        private const val EVENT_SELECT_HIKING_ROUTE = "select_hiking_route"
        private const val EVENT_SELECT_MY_LOCATION = "select_my_location"
        private const val EVENT_SELECT_ROUTE_PLANNER = "select_route_planner"
        private const val EVENT_SELECT_ROUTE_PLANNER_PICK_LOCATION = "select_route_planner_pick_location"
        private const val EVENT_SELECT_ROUTE_PLANNER_MY_LOCATION = "select_route_planner_my_location"
        private const val EVENT_SELECT_ROUTE_PLANNER_DONE = "select_route_planner_done"
        private const val EVENT_SELECT_MAPS_DIRECTIONS = "select_maps_directions"
        private const val EVENT_GPX_IMPORT_CLICKED = "gpx_import_clicked"
        private const val EVENT_GPX_IMPORTED = "gpx_imported"
        private const val EVENT_GPX_IMPORTED_BY_INTENT = "gpx_imported_by_intent"
        private const val EVENT_GPX_IMPORTED_BY_FILE_EXPLORER = "gpx_imported_by_file_explorer"
        private const val EVENT_LAYER_MAPNIK_SELECTED = "layer_mapnik_selected"
        private const val EVENT_LAYER_OPEN_TOPO_SELECTED = "layer_open_topo_selected"
        private const val EVENT_LAYER_HIKING_SELECTED = "layer_hiking_selected"
        private const val EVENT_LAYER_GPX_SELECTED = "layer_gpx_selected"
        private const val EVENT_SELECT_SETTINGS = "select_settings"
        private const val EVENT_SELECT_SETTINGS_MAP_SCALE_INFO = "select_settings_map_scale_info"
        private const val EVENT_SELECT_SETTINGS_MAP_SCALE = "select_settings_map_scale"
        private const val EVENT_SELECT_SETTINGS_EMAIL = "select_settings_email"
        private const val EVENT_SELECT_SETTINGS_GITHUB = "select_settings_github"
        private const val EVENT_SELECT_SETTINGS_GOOGLE_PLAY_REVIEW = "select_settings_gps_review"
        private const val EVENT_SELECT_SETTINGS_THEME_LIGHT = "select_settings_theme_light"
        private const val EVENT_SELECT_SETTINGS_THEME_DARK = "select_settings_theme_dark"
        private const val EVENT_SELECT_GPX_HISTORY = "select_gpx_history"
        private const val EVENT_OPEN_GPX_HISTORY_ITEM = "open_gpx_history_item"
        private const val EVENT_SHARE_GPX_HISTORY_ITEM = "share_gpx_history_item"
        private const val EVENT_SELECT_COPYRIGHT = "select_copyright"
        private const val EVENT_DESTROYED = "destroyed"

        private const val PARAM_SEARCH_PLACE_TEXT = "search_place_text"
        private const val PARAM_SELECTED_PLACE_NAME = "selected_place_name"
        private const val PARAM_SELECTED_LANDSCAPE = "selected_landscape"
        private const val PARAM_SELECTED_PLACE_DETAILS = "selected_place_details"
        private const val PARAM_LOAD_HIKING_ROUTE_FOR = "load_hiking_route_for"
        private const val PARAM_SELECTED_HIKING_ROUTE_DETAILS = "selected_hiking_route_details"
        private const val PARAM_NAVIGATION_PLACE_NAME = "navigation_place_name"
        private const val PARAM_MAP_SCALE_PERCENTAGE = "map_scale_percentage"
        private const val PARAM_TILE_DOWNLOAD_REQUEST_COUNTER = "tile_download_request_counter"
        private const val PARAM_TILE_DOWNLOAD_OUT_OF_MEMORY = "tile_download_out_of_memory"
        private const val PARAM_TILE_DOWNLOAD_ERRORS = "tile_download_errors"
        private const val PARAM_TILE_CACHE_OUT_OF_MEMORY = "tile_cache_out_of_memory"
        private const val PARAM_IMPORTED_GPX_NAME = "imported_gpx_name"
        private const val PARAM_ROUTE_PLANNER_WAYPOINT_COUNT = "route_planner_waypoint_count"
        private const val PARAM_ROUTE_PLANNER_DISTANCE = "route_planner_distance"
    }

    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun placeFinderPlaceClicked(searchText: String, placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE) {
            param(PARAM_SEARCH_PLACE_TEXT, searchText)
            param(PARAM_SELECTED_PLACE_NAME, placeName)
        }
    }

    override fun loadLandscapeClicked(placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE) {
            param(PARAM_SELECTED_LANDSCAPE, placeName)
        }
    }

    override fun landscapeKirandulastippekClicked(placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE_KIRANDULASTIPPEK) {
            param(PARAM_SELECTED_LANDSCAPE, placeName)
        }
    }

    override fun landscapeKirandulastippekInfoClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE_KIRANDULASTIPPEK_INFO, null)
    }

    override fun landscapeTermeszetjaroClicked(placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE_TERMESZETJARO) {
            param(PARAM_SELECTED_LANDSCAPE, placeName)
        }
    }

    override fun landscapeTermeszetjaroInfoClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE_TERMESZETJARO_INFO, null)
    }

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_DETAILS) {
            param(PARAM_SELECTED_PLACE_DETAILS, placeName)
        }
    }

    override fun loadHikingRoutesClicked(placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SEARCH_HIKING_ROUTES) {
            param(PARAM_LOAD_HIKING_ROUTE_FOR, placeName)
        }
    }

    override fun loadHikingRouteDetailsClicked(routeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKING_ROUTE) {
            param(PARAM_SELECTED_HIKING_ROUTE_DETAILS, routeName)
        }
    }

    override fun myLocationClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_MY_LOCATION, null)
    }

    override fun routePlannerClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER, null)
    }

    override fun routePlannerPickLocationClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER_PICK_LOCATION, null)
    }

    override fun routePlannerMyLocationClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER_MY_LOCATION, null)
    }

    override fun routePlanSaved(routePlan: RoutePlanUiModel) {
        val waypointCount = routePlan.wayPoints.size
        val distance = routePlan.distanceText.formatArgs
            .firstOrNull()
            ?.toString()
            ?.toDoubleOrNull()

        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER_DONE) {
            param(PARAM_ROUTE_PLANNER_WAYPOINT_COUNT, waypointCount.toLong())
            if (distance != null) {
                param(PARAM_ROUTE_PLANNER_DISTANCE, distance)
            }
        }
    }

    override fun googleMapsClicked(destinationPlaceName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_MAPS_DIRECTIONS) {
            param(PARAM_NAVIGATION_PLACE_NAME, destinationPlaceName)
        }
    }

    override fun gpxImportClicked() {
        firebaseAnalytics.logEvent(EVENT_GPX_IMPORT_CLICKED, null)
    }

    override fun gpxImported(fileName: String) {
        val nameWithoutExtension = fileName.substringBeforeLast(".")

        firebaseAnalytics.logEvent(EVENT_GPX_IMPORTED) {
            param(PARAM_IMPORTED_GPX_NAME, nameWithoutExtension)
        }
    }

    override fun gpxImportedByIntent() {
        firebaseAnalytics.logEvent(EVENT_GPX_IMPORTED_BY_INTENT, null)
    }

    override fun gpxImportedByFileExplorer() {
        firebaseAnalytics.logEvent(EVENT_GPX_IMPORTED_BY_FILE_EXPLORER, null)
    }

    override fun onLayerSelected(layerType: LayerType) {
        firebaseAnalytics.logEvent(
            when (layerType) {
                LayerType.MAPNIK -> EVENT_LAYER_MAPNIK_SELECTED
                LayerType.OPEN_TOPO -> EVENT_LAYER_OPEN_TOPO_SELECTED
                LayerType.HUNGARIAN_HIKING_LAYER -> EVENT_LAYER_HIKING_SELECTED
                LayerType.GPX -> EVENT_LAYER_GPX_SELECTED
            },
            null
        )
    }

    override fun settingsClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS, null)
    }

    override fun settingsMapScaleInfoClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_MAP_SCALE_INFO, null)
    }

    override fun settingsMapScaleSet(mapScalePercentage: Long) {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_MAP_SCALE) {
            param(PARAM_MAP_SCALE_PERCENTAGE, mapScalePercentage)
        }
    }

    override fun settingsEmailClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_EMAIL, null)
    }

    override fun settingsGitHubClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_GITHUB, null)
    }

    override fun settingsGooglePlayReviewClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_GOOGLE_PLAY_REVIEW, null)
    }

    override fun settingsThemeClicked(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_THEME_LIGHT, null)
            Theme.DARK -> firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_THEME_DARK, null)
            Theme.SYSTEM -> {
                /** no-op **/
            }
        }
    }

    override fun gpxHistoryClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_HISTORY, null)
    }

    override fun gpxHistoryItemOpened() {
        firebaseAnalytics.logEvent(EVENT_OPEN_GPX_HISTORY_ITEM, null)
    }

    override fun gpxHistoryItemShared() {
        firebaseAnalytics.logEvent(EVENT_SHARE_GPX_HISTORY_ITEM, null)
    }

    override fun copyrightClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_COPYRIGHT, null)
    }

    override fun destroyed() {
        firebaseAnalytics.logEvent(EVENT_DESTROYED) {
            param(PARAM_TILE_DOWNLOAD_REQUEST_COUNTER, CounterProvider.tileDownloadRequestCounter.toLong())
            param(PARAM_TILE_DOWNLOAD_OUT_OF_MEMORY, Counters.countOOM.toLong())
            param(PARAM_TILE_DOWNLOAD_ERRORS, Counters.tileDownloadErrors.toLong())
            param(PARAM_TILE_CACHE_OUT_OF_MEMORY, Counters.fileCacheOOM.toLong())
        }
    }

}
