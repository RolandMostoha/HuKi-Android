package hu.mostoha.mobile.android.huki.osmdroid.tiles

import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider

object FakeHikingTileUrlProvider : HikingTileUrlProvider {

    override fun getHikingTileUrl(storageKey: String): String {
        return ""
    }

}
