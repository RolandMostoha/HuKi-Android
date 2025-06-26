package hu.mostoha.mobile.android.huki.configuration

import javax.inject.Inject

class TestAppConfiguration @Inject constructor() : AppConfiguration {

    override fun getNetworkDebounceDelay(): Long = 0L

    override fun getPlaceHistoryMaxRowCount(): Int = 10

}
