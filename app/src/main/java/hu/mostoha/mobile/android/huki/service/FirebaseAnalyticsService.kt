package hu.mostoha.mobile.android.huki.service

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.CounterProvider
import org.osmdroid.tileprovider.util.Counters
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor() : AnalyticsService {

    companion object {
        private const val EVENT_SELECT_PLACE = "select_place"
        private const val EVENT_SELECT_LANDSCAPE = "select_landscape"
        private const val EVENT_SELECT_PLACE_DETAILS = "select_place_details"
        private const val EVENT_SEARCH_HIKING_ROUTES = "search_hiking_routes"
        private const val EVENT_SELECT_HIKING_ROUTE = "select_hiking_route"
        private const val EVENT_SELECT_MY_LOCATION = "select_my_location"
        private const val EVENT_SELECT_MAPS_DIRECTIONS = "select_maps_directions"
        private const val EVENT_SELECT_COPYRIGHT = "select_copyright"
        private const val EVENT_DESTROYED = "destroyed"

        private const val PARAM_SEARCH_PLACE_TEXT = "search_place_text"
        private const val PARAM_SELECTED_PLACE_NAME = "selected_place_name"
        private const val PARAM_SELECTED_LANDSCAPE = "selected_landscape"
        private const val PARAM_SELECTED_PLACE_DETAILS = "selected_place_details"
        private const val PARAM_LOAD_HIKING_ROUTE_FOR = "load_hiking_route_for"
        private const val PARAM_SELECTED_HIKING_ROUTE_DETAILS = "selected_hiking_route_details"
        private const val PARAM_NAVIGATION_PLACE_NAME = "navigation_place_name"
        private const val PARAM_TILE_DOWNLOAD_REQUEST_COUNTER = "tile_download_request_counter"
        private const val PARAM_TILE_DOWNLOAD_OUT_OF_MEMORY = "tile_download_out_of_memory"
        private const val PARAM_TILE_DOWNLOAD_ERRORS = "tile_download_errors"
        private const val PARAM_TILE_CACHE_OUT_OF_MEMORY = "tile_cache_out_of_memory"
    }

    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun searchBarPlaceClicked(searchText: String, placeName: String) {
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

    override fun navigationClicked(destinationPlaceName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_MAPS_DIRECTIONS) {
            param(PARAM_NAVIGATION_PLACE_NAME, destinationPlaceName)
        }
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
