package hu.mostoha.mobile.android.huki.configuration

class TestAppConfiguration : AppConfiguration {

    override fun getNetworkDebounceDelay(): Long = 1L

    override fun getPlaceHistoryMaxRowCount(): Int = 10

}
