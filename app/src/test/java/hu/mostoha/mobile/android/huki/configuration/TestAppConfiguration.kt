package hu.mostoha.mobile.android.huki.configuration

class TestAppConfiguration : AppConfiguration {

    override fun getSearchQueryDelay(): Long = 1L

    override fun getPlaceHistoryMaxRowCount(): Int = 10

}
