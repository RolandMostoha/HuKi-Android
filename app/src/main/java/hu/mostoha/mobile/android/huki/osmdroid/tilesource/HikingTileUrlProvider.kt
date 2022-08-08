package hu.mostoha.mobile.android.huki.osmdroid.tilesource

interface HikingTileUrlProvider {

    fun getHikingTileUrl(storageKey: String): String

}
