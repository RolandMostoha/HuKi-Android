package hu.mostoha.mobile.android.huki.ui.home.shared

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.util.EventSharedViewModel
import javax.inject.Inject

@HiltViewModel
class MapTouchEventSharedViewModel @Inject constructor() : EventSharedViewModel<MapTouchEvents>()

enum class MapTouchEvents {
    MAP_TOUCHED
}
