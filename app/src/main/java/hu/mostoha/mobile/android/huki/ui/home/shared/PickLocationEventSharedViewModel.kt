package hu.mostoha.mobile.android.huki.ui.home.shared

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.util.EventSharedViewModel
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class PickLocationEventSharedViewModel @Inject constructor() : EventSharedViewModel<PickLocationEvents>()

sealed class PickLocationEvents {

    data object LocationPickEnabled : PickLocationEvents()

    data object LocationPickDisabled : PickLocationEvents()

    data object RoutePlannerPickEnabled : PickLocationEvents()

    data class RoutePlannerPickEnded(val geoPoint: GeoPoint) : PickLocationEvents()

}
