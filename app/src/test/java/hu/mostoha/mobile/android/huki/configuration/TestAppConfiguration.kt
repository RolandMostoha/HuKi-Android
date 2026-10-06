package hu.mostoha.mobile.android.huki.configuration

import hu.mostoha.mobile.android.huki.model.domain.RoutingMode

class TestAppConfiguration : AppConfiguration {

    override fun getNetworkDebounceDelay(): Long = 1L

    override fun getPlaceHistoryMaxRowCount(): Int = 10

    override fun getRoutingMode(): RoutingMode = RoutingMode.AUTO

}
