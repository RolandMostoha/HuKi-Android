package hu.mostoha.mobile.android.huki.service

import com.android.billingclient.api.BillingClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import hu.mostoha.mobile.android.huki.billing.fieldName
import hu.mostoha.mobile.android.huki.extensions.removeFileExtension
import hu.mostoha.mobile.android.huki.model.domain.GpxType
import hu.mostoha.mobile.android.huki.model.domain.LayerType
import hu.mostoha.mobile.android.huki.model.domain.OktType
import hu.mostoha.mobile.android.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.model.domain.RoutePlanType
import hu.mostoha.mobile.android.huki.model.domain.Theme
import hu.mostoha.mobile.android.huki.model.ui.BillingAction
import hu.mostoha.mobile.android.huki.model.ui.RoutePlanUiModel
import hu.mostoha.mobile.android.huki.util.toAnalyticsEvent
import javax.inject.Inject

@Suppress("TooManyFunctions")
class FirebaseAnalyticsService @Inject constructor() : AnalyticsService {

    companion object {
        private const val EVENT_SELECT_PLACE = "select_place"
        private const val EVENT_SELECT_PLACE_FROM_HISTORY = "select_place_from_history"
        private const val EVENT_SELECT_LANDSCAPE = "select_landscape"
        private const val EVENT_SELECT_OKT = "select_okt"
        private const val EVENT_SELECT_OKT_ROUTE = "select_okt_route"
        private const val EVENT_SELECT_OKT_ROUTE_LINK = "select_okt_link"
        private const val EVENT_SELECT_OKT_ROUTE_EDGE_POINT = "select_okt_edge_point"
        private const val EVENT_SELECT_OKT_WAYPOINT = "select_okt_waypoint"
        private const val EVENT_OKT_GPX_IMPORTED = "okt_gpx_imported"
        private const val EVENT_SELECT_PLACE_DETAILS_HIKE_RECOMMENDER = "select_place_details_hike_recommender"
        private const val EVENT_SELECT_HIKE_RECOMMENDER_KIRANDULASTIPPEK = "select_kirandulastippek"
        private const val EVENT_SELECT_HIKE_RECOMMENDER_TERMESZETJARO = "select_termeszetjaro"
        private const val EVENT_SELECT_HIKE_RECOMMENDER_INFO_CLOSE = "select_hike_recommender_info_close"
        private const val EVENT_SELECT_PLACE_DETAILS = "select_place_details"
        private const val EVENT_SEARCH_HIKING_ROUTES = "search_hiking_routes"
        private const val EVENT_SELECT_HIKING_ROUTE = "select_hiking_route"
        private const val EVENT_SELECT_MY_LOCATION = "select_my_location"
        private const val EVENT_SELECT_ROUTE_PLANNER = "select_route_planner"
        private const val EVENT_SELECT_ROUTE_PLANNER_PICK_LOCATION = "select_route_planner_pick_location"
        private const val EVENT_SELECT_ROUTE_PLANNER_MY_LOCATION = "select_route_planner_my_location"
        private const val EVENT_SELECT_ROUTE_PLANNER_DONE = "select_route_planner_done"
        private const val EVENT_SELECT_ROUTE_PLANNER_COMMENT_DONE = "select_route_planner_comment_done"
        private const val EVENT_SELECT_ROUTE_PLANNER_HIKE_TYPE_TEMPLATE = "select_route_planner_%s"
        private const val EVENT_SELECT_MAPS_DIRECTIONS = "select_maps_directions"
        private const val EVENT_GPX_IMPORT_CLICKED = "gpx_import_clicked"
        private const val EVENT_GPX_IMPORTED = "gpx_imported"
        private const val EVENT_GPX_IMPORTED_BY_INTENT = "gpx_imported_by_intent"
        private const val EVENT_GPX_IMPORTED_BY_FILE_EXPLORER = "gpx_imported_by_file_explorer"
        private const val EVENT_SELECT_GPX_START = "select_gpx_start"
        private const val EVENT_SELECT_GPX_VISIBILITY = "select_gpx_visibility"
        private const val EVENT_SELECT_GPX_SHARE = "select_gpx_share"
        private const val EVENT_SELECT_GPX_WAYPOINT = "select_gpx_waypoint"
        private const val EVENT_GPX_WAYPOINTS_ONLY_IMPORTED = "gpx_imported_waypoints_only"
        private const val EVENT_LAYERS_SELECTED = "select_layers"
        private const val EVENT_LAYER_SELECTED_TEMPLATE = "layer_%s_selected"
        private const val EVENT_SELECT_SETTINGS = "select_settings"
        private const val EVENT_SELECT_SETTINGS_MAP_SCALE_INFO = "select_settings_map_scale_info"
        private const val EVENT_SELECT_SETTINGS_MAP_SCALE = "select_settings_map_scale"
        private const val EVENT_SELECT_SETTINGS_EMAIL = "select_settings_email"
        private const val EVENT_SELECT_SETTINGS_GITHUB = "select_settings_github"
        private const val EVENT_SELECT_SETTINGS_GOOGLE_PLAY_REVIEW = "select_settings_gps_review"
        private const val EVENT_SELECT_SETTINGS_THEME_LIGHT = "select_settings_theme_light"
        private const val EVENT_SELECT_SETTINGS_THEME_DARK = "select_settings_theme_dark"
        private const val EVENT_SELECT_SETTINGS_OFFLINE_MODE_INFO = "select_settings_offline_mode_info"
        private const val EVENT_SELECT_HISTORY = "select_history"
        private const val EVENT_SELECT_GPX_HISTORY = "select_gpx_history"
        private const val EVENT_OPEN_GPX_HISTORY_ITEM = "open_gpx_history_item"
        private const val EVENT_OPEN_ROUTE_PLANNER_HISTORY_ITEM = "open_route_planner_history_item"
        private const val EVENT_SHARE_GPX_HISTORY_ITEM = "share_gpx_history_item"
        private const val EVENT_DELETE_GPX_HISTORY_ITEM = "delete_gpx_history_item"
        private const val EVENT_RENAME_GPX_HISTORY_ITEM = "rename_gpx_history_item"
        private const val EVENT_OPEN_PLACE_HISTORY_ITEM = "open_place_history_item"
        private const val EVENT_DELETE_PLACE_HISTORY_ITEM = "delete_place_history_item"
        private const val EVENT_PLACE_REQUESTED_MY_LOCATION = "place_requested_my_location"
        private const val EVENT_PLACE_REQUESTED_PICK_LOCATION = "place_requested_pick_location"
        private const val EVENT_PLACE_REQUESTED_OKT_WAYPOINT = "place_requested_okt_waypoint"
        private const val EVENT_PLACE_REQUESTED_GPX_WAYPOINT = "place_requested_gpx_waypoint"
        private const val EVENT_SELECT_COPYRIGHT = "select_copyright"
        private const val EVENT_SELECT_HIKE_MODE = "select_hike_mode"
        private const val EVENT_SELECT_LIVE_COMPASS = "select_live_compass"
        private const val EVENT_SELECT_SUPPORT = "select_support"
        private const val EVENT_SELECT_SUPPORT_EMAIL = "select_support_email"
        private const val EVENT_SELECT_SUPPORT_RECURRING_LEVEL_1 = "select_support_recurring_level_1"
        private const val EVENT_SELECT_SUPPORT_RECURRING_LEVEL_2 = "select_support_recurring_level_2"
        private const val EVENT_SELECT_SUPPORT_ONE_TIME_LEVEL_1 = "select_support_one_time_level_1"
        private const val EVENT_SELECT_SUPPORT_ONE_TIME_LEVEL_2 = "select_support_one_time_level_2"
        private const val EVENT_SELECT_ALL_OSM_DATA = "select_all_osm_data"
        private const val EVENT_VIEW_NEW_FEATURES = "view_new_features"
        private const val EVENT_SELECT_PLACE_CATEGORY = "select_place_category"

        private const val PARAM_SEARCH_PLACE_TEXT = "search_place_text"
        private const val PARAM_SELECTED_PLACE_NAME = "selected_place_name"
        private const val PARAM_SELECTED_LANDSCAPE = "selected_landscape"
        private const val PARAM_SELECTED_PLACE_DETAILS = "selected_place_details"
        private const val PARAM_SELECTED_HIKING_ROUTE_DETAILS = "selected_hiking_route_details"
        private const val PARAM_MAP_SCALE_PERCENTAGE = "map_scale_percentage"
        private const val PARAM_IMPORTED_GPX_NAME = "imported_gpx_name"
        private const val PARAM_ROUTE_PLANNER_WAYPOINT_COUNT = "route_planner_waypoint_count"
        private const val PARAM_ROUTE_PLANNER_DISTANCE = "route_planner_distance"
        private const val PARAM_OKT_ID = "okt_id"
        private const val PARAM_NEW_FEATURES_VERSION = "version"

        private val MAP_SCALE_PERCENTAGE_RANGES = listOf(
            1..99,
            100..150,
            151..250,
            251..300
        )
        private val ROUTE_PLANNER_DISTANCE_RANGES = listOf(
            0.0..2.0,
            3.0..5.0,
            6.0..10.0,
            11.0..20.0,
            21.0..100.0,
        )
        private val ROUTE_PLANNER_WAYPOINT_COUNT_RANGES = listOf(
            2..3,
            4..6,
            7..10
        )
    }

    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun placeFinderPlaceClicked(searchText: String, placeName: String, isFromHistory: Boolean) {
        if (isFromHistory) {
            firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_FROM_HISTORY, null)
        }

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

    override fun placeDetailsFinderClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_DETAILS_HIKE_RECOMMENDER, null)
    }

    override fun hikeRecommenderKirandulastippekClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKE_RECOMMENDER_KIRANDULASTIPPEK, null)
    }

    override fun hikeRecommenderTermeszetjaroClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKE_RECOMMENDER_TERMESZETJARO, null)
    }

    override fun hikeRecommenderInfoCloseClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKE_RECOMMENDER_INFO_CLOSE, null)
    }

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_DETAILS) {
            param(PARAM_SELECTED_PLACE_DETAILS, placeName)
        }
    }

    override fun loadHikingRoutesClicked() {
        firebaseAnalytics.logEvent(EVENT_SEARCH_HIKING_ROUTES, null)
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

    override fun routePlannerCommentDone() {
        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER_COMMENT_DONE, null)
    }

    override fun routePlannerTypeClicked(planType: RoutePlanType) {
        val eventName = EVENT_SELECT_ROUTE_PLANNER_HIKE_TYPE_TEMPLATE.format(
            planType.javaClass.simpleName.lowercase()
        )
        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun routePlanSaved(routePlan: RoutePlanUiModel) {
        val waypointCountParam = ROUTE_PLANNER_WAYPOINT_COUNT_RANGES.toAnalyticsEvent(routePlan.wayPoints.size)
        val distanceParam = routePlan.distanceText.formatArgs
            .firstOrNull()
            ?.toString()
            ?.toDoubleOrNull()
            ?.let { ROUTE_PLANNER_DISTANCE_RANGES.toAnalyticsEvent(it) }

        firebaseAnalytics.logEvent(EVENT_SELECT_ROUTE_PLANNER_DONE) {
            if (waypointCountParam != null) {
                param(PARAM_ROUTE_PLANNER_WAYPOINT_COUNT, waypointCountParam)
            }
            if (distanceParam != null) {
                param(PARAM_ROUTE_PLANNER_DISTANCE, distanceParam)
            }
        }
    }

    override fun googleMapsClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_MAPS_DIRECTIONS, null)
    }

    override fun gpxImportClicked() {
        firebaseAnalytics.logEvent(EVENT_GPX_IMPORT_CLICKED, null)
    }

    override fun gpxImported(fileName: String) {
        val nameWithoutExtension = fileName.removeFileExtension()

        if (nameWithoutExtension.contains("okt_", ignoreCase = true) ||
            nameWithoutExtension.contains("okt-", ignoreCase = true)
        ) {
            oktGpxImported(nameWithoutExtension)
        }

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

    override fun gpxWaypointClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_WAYPOINT, null)
    }

    override fun gpxDetailsStartClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_START, null)
    }

    override fun gpxDetailsVisibilityClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_VISIBILITY, null)
    }

    override fun gpxDetailsShareClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_SHARE, null)
    }

    override fun gpxDetailsWaypointsOnlyImported() {
        firebaseAnalytics.logEvent(EVENT_GPX_WAYPOINTS_ONLY_IMPORTED, null)
    }

    override fun layersClicked() {
        firebaseAnalytics.logEvent(EVENT_LAYERS_SELECTED, null)
    }

    override fun onLayerSelected(layerType: LayerType) {
        firebaseAnalytics.logEvent(
            EVENT_LAYER_SELECTED_TEMPLATE.format(EVENT_LAYER_SELECTED_TEMPLATE, layerType.name.lowercase()),
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
        val mapScaleParam = MAP_SCALE_PERCENTAGE_RANGES.toAnalyticsEvent(mapScalePercentage.toInt())

        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_MAP_SCALE) {
            if (mapScaleParam != null) {
                param(PARAM_MAP_SCALE_PERCENTAGE, mapScaleParam)
            }
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
            Theme.SYSTEM -> Unit
        }
    }

    override fun settingsOfflineModeInfoClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SETTINGS_OFFLINE_MODE_INFO, null)
    }

    override fun historyClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_HISTORY, null)
    }

    override fun gpxHistoryClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_GPX_HISTORY, null)
    }

    override fun gpxHistoryItemOpened(gpxType: GpxType) {
        when (gpxType) {
            GpxType.ROUTE_PLANNER -> firebaseAnalytics.logEvent(EVENT_OPEN_ROUTE_PLANNER_HISTORY_ITEM, null)
            GpxType.EXTERNAL -> firebaseAnalytics.logEvent(EVENT_OPEN_GPX_HISTORY_ITEM, null)
        }
    }

    override fun gpxHistoryItemShared() {
        firebaseAnalytics.logEvent(EVENT_SHARE_GPX_HISTORY_ITEM, null)
    }

    override fun gpxHistoryItemDelete() {
        firebaseAnalytics.logEvent(EVENT_DELETE_GPX_HISTORY_ITEM, null)
    }

    override fun gpxHistoryItemRename() {
        firebaseAnalytics.logEvent(EVENT_RENAME_GPX_HISTORY_ITEM, null)
    }

    override fun placeHistoryItemOpen() {
        firebaseAnalytics.logEvent(EVENT_OPEN_PLACE_HISTORY_ITEM, null)
    }

    override fun placeHistoryItemDelete() {
        firebaseAnalytics.logEvent(EVENT_DELETE_PLACE_HISTORY_ITEM, null)
    }

    override fun oktClicked(oktType: OktType) {
        val eventName = "${EVENT_SELECT_OKT}_${oktType.name.lowercase()}"
        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun oktRouteClicked(oktId: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_OKT_ROUTE) {
            param(PARAM_OKT_ID, oktId)
        }
    }

    override fun oktRouteLinkClicked(oktId: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_OKT_ROUTE_LINK) {
            param(PARAM_OKT_ID, oktId)
        }
    }

    override fun oktRouteEdgePointClicked(oktId: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_OKT_ROUTE_EDGE_POINT) {
            param(PARAM_OKT_ID, oktId)
        }
    }

    override fun oktGpxImported(fileName: String) {
        firebaseAnalytics.logEvent(EVENT_OKT_GPX_IMPORTED) {
            param(PARAM_IMPORTED_GPX_NAME, fileName)
        }
    }

    override fun oktWaypointClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_OKT_WAYPOINT, null)
    }

    override fun myLocationPlaceRequested() {
        firebaseAnalytics.logEvent(EVENT_PLACE_REQUESTED_MY_LOCATION, null)
    }

    override fun pickLocationPlaceRequested() {
        firebaseAnalytics.logEvent(EVENT_PLACE_REQUESTED_PICK_LOCATION, null)
    }

    override fun oktWaypointPlaceRequested() {
        firebaseAnalytics.logEvent(EVENT_PLACE_REQUESTED_OKT_WAYPOINT, null)
    }

    override fun gpxWaypointPlaceRequested() {
        firebaseAnalytics.logEvent(EVENT_PLACE_REQUESTED_GPX_WAYPOINT, null)
    }

    override fun copyrightClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_COPYRIGHT, null)
    }

    override fun hikeModeClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKE_MODE, null)
    }

    override fun liveCompassClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_LIVE_COMPASS, null)
    }

    override fun supportClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT, null)
    }

    override fun supportEmailClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT_EMAIL, null)
    }

    override fun supportRecurringLevel1Clicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT_RECURRING_LEVEL_1, null)
    }

    override fun supportRecurringLevel2Clicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT_RECURRING_LEVEL_2, null)
    }

    override fun supportOneTimeLevel1Clicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT_ONE_TIME_LEVEL_1, null)
    }

    override fun supportOneTimeLevel2Clicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_SUPPORT_ONE_TIME_LEVEL_2, null)
    }

    override fun newFeaturesSeen(version: String) {
        firebaseAnalytics.logEvent(EVENT_VIEW_NEW_FEATURES) {
            param(PARAM_NEW_FEATURES_VERSION, version)
        }
    }

    override fun billingEvent(billingAction: BillingAction, billingResponseCode: Int) {
        val isStartupAction = billingAction == BillingAction.START_CONNECTION ||
            billingAction == BillingAction.QUERY_PRODUCTS ||
            billingAction == BillingAction.QUERY_PURCHASES
        val isOkResponse = BillingClient.BillingResponseCode.OK == billingResponseCode
        val billingResponseFieldName = billingResponseCode.fieldName()?.lowercase()

        if ((isStartupAction && isOkResponse) || billingResponseFieldName == null) {
            return
        }

        val eventName = "billing_${billingAction.name.lowercase()}_$billingResponseFieldName"

        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun placeCategoryFabClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_CATEGORY, null)
    }

    override fun placeCategoryClicked(placeCategory: PlaceCategory) {
        firebaseAnalytics.logEvent("${EVENT_SELECT_PLACE_CATEGORY}_${placeCategory.name.lowercase()}", null)
    }

    @Suppress("MagicNumber")
    override fun placeCategoryLoaded(numberOfPlaces: Int) {
        val eventName = when {
            numberOfPlaces <= 0 -> return
            numberOfPlaces < 50 -> "view_place_category_less_50"
            numberOfPlaces in 50..100 -> "view_place_category_50_to_100"
            else -> "view_place_category_above_100"
        }

        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun allOsmDataClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_ALL_OSM_DATA, null)
    }

}
