package hu.mostoha.mobile.android.huki.configuration

import javax.inject.Inject

class HukiAppConfiguration @Inject constructor() : AppConfiguration {

    override fun getSearchQueryDelay() = 1200L

}
