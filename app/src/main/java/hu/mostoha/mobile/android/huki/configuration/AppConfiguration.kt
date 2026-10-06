package hu.mostoha.mobile.android.huki.configuration

import hu.mostoha.mobile.android.huki.model.domain.RoutingMode

interface AppConfiguration {

    fun getNetworkDebounceDelay(): Long

    fun getPlaceHistoryMaxRowCount(): Int

    fun getRoutingMode(): RoutingMode

}
