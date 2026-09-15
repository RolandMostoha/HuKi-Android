package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.RawRes
import hu.mostoha.mobile.android.huki.R

enum class SupportAnimation(@RawRes val rawRes: Int) {
    GUIDE(R.raw.lottie_support_guide),
    TOURIST(R.raw.lottie_support_tourist),
    BACKPACKER(R.raw.lottie_support_backpacker),
    MUSHROOMER(R.raw.lottie_support_mushroomer),
    FOREST_REST(R.raw.lottie_support_forest_rest),
    TRAVEL(R.raw.lottie_support_travel),
    MOUNTAIN_BRIDGE(R.raw.lottie_support_mountain_bridge),
    EARTH(R.raw.lottie_support_earth),
}
