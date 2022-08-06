package hu.mostoha.mobile.android.huki.service

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.PlaceType
import hu.mostoha.mobile.android.huki.osmdroid.CounterProvider
import org.osmdroid.tileprovider.util.Counters
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor() : AnalyticsService {

    companion object {
        private const val EVENT_SEARCH_PLACE = "search_place"
        private const val EVENT_SELECT_PLACE = "select_place"
        private const val EVENT_SELECT_LANDSCAPE = "select_landscape"
        private const val EVENT_SELECT_PLACE_DETAILS = "select_place_details"
        private const val EVENT_SEARCH_HIKING_ROUTES = "search_hiking_routes"
        private const val EVENT_SELECT_HIKING_ROUTE = "select_hiking_route"
        private const val EVENT_SELECT_MY_LOCATION = "select_my_location"
        private const val EVENT_SELECT_MAPS_DIRECTIONS = "select_maps_directions"
        private const val EVENT_SELECT_COPYRIGHT = "select_copyright"
        private const val EVENT_DESTROYED = "destroyed"

        private const val PARAM_BOUNDS = "bounds"
        private const val PARAM_TILE_DOWNLOAD_REQUEST_COUNTER = "tile_download_request_counter"
        private const val PARAM_TILE_DOWNLOAD_OUT_OF_MEMORY = "tile_download_out_of_memory"
        private const val PARAM_TILE_DOWNLOAD_ERRORS = "tile_download_errors"
        private const val PARAM_TILE_CACHE_OUT_OF_MEMORY = "tile_cache_out_of_memory"
        private const val PARAM_TILE_CACHE_HIT = "tile_cache_hit"
    }

    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun searchBarPlaceClicked(searchText: String, placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SEARCH_PLACE) {
            param(FirebaseAnalytics.Param.VALUE, searchText)
        }
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE) {
            param(FirebaseAnalytics.Param.VALUE, placeName)
        }
    }

    override fun loadLandscapeClicked(placeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_LANDSCAPE) {
            param(FirebaseAnalytics.Param.VALUE, placeName)
        }
    }

    override fun loadPlaceDetailsClicked(placeName: String, placeType: PlaceType) {
        firebaseAnalytics.logEvent(EVENT_SELECT_PLACE_DETAILS) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, placeType.name)
            param(FirebaseAnalytics.Param.VALUE, placeName)
        }
    }

    override fun loadHikingRoutesClicked(placeName: String, boundingBox: BoundingBox) {
        firebaseAnalytics.logEvent(EVENT_SEARCH_HIKING_ROUTES) {
            param(FirebaseAnalytics.Param.VALUE, placeName)
            param(PARAM_BOUNDS, boundingBox.toString())
        }
    }

    override fun loadHikingRouteDetailsClicked(routeName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_HIKING_ROUTE) {
            param(FirebaseAnalytics.Param.VALUE, routeName)
        }
    }

    override fun myLocationClicked() {
        firebaseAnalytics.logEvent(EVENT_SELECT_MY_LOCATION, null)
    }

    override fun navigationClicked(destinationPlaceName: String) {
        firebaseAnalytics.logEvent(EVENT_SELECT_MAPS_DIRECTIONS) {
            param(FirebaseAnalytics.Param.VALUE, destinationPlaceName)
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
            param(PARAM_TILE_CACHE_HIT, Counters.fileCacheHit.toLong())
        }
    }

}
