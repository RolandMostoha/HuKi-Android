package hu.mostoha.mobile.android.huki.configuration

import javax.inject.Inject

class HukiAppConfiguration @Inject constructor() : AppConfiguration {

    override fun getNetworkDebounceDelay() = 1200L

    override fun getPlaceHistoryMaxRowCount(): Int = 1000

}
