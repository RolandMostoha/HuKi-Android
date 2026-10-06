package hu.mostoha.mobile.android.huki.configuration

import hu.mostoha.mobile.android.huki.BuildConfig
import hu.mostoha.mobile.android.huki.model.domain.RoutingMode
import hu.mostoha.mobile.android.huki.util.FeatureFlags
import javax.inject.Inject

class HukiAppConfiguration @Inject constructor() : AppConfiguration {

    override fun getNetworkDebounceDelay() = 1200L

    override fun getPlaceHistoryMaxRowCount(): Int = 1000

    override fun getRoutingMode(): RoutingMode {
        return if (BuildConfig.DEBUG) FeatureFlags.DEBUG_ROUTING_MODE else RoutingMode.AUTO
    }

}
