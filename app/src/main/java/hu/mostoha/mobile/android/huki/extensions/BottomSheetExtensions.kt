package hu.mostoha.mobile.android.huki.extensions

import com.google.android.material.bottomsheet.BottomSheetBehavior

fun BottomSheetBehavior<*>.switchState() {
    if (state == BottomSheetBehavior.STATE_COLLAPSED) {
        state = BottomSheetBehavior.STATE_HIDDEN
    } else if (state == BottomSheetBehavior.STATE_HIDDEN) {
        state = BottomSheetBehavior.STATE_COLLAPSED
    }
}

fun BottomSheetBehavior<*>.hide() {
    if (state != BottomSheetBehavior.STATE_HIDDEN) {
        state = BottomSheetBehavior.STATE_HIDDEN
    }
}

fun BottomSheetBehavior<*>.collapse() {
    if (state != BottomSheetBehavior.STATE_COLLAPSED) {
        state = BottomSheetBehavior.STATE_COLLAPSED
    }
}