package hu.mostoha.mobile.android.huki.osmdroid.tiles

object FakeHikingTileUrlProvider : HikingTileUrlProvider {

    override fun getHikingTileUrl(storageKey: String): String {
        return ""
    }

}
