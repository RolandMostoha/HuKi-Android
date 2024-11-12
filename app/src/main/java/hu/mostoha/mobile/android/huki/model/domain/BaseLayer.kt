package hu.mostoha.mobile.android.huki.model.domain

import hu.mostoha.mobile.android.huki.osmdroid.tilesource.GoogleSateliteTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.MapnikTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.MerreTekerjekTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.TuHuTileSource
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

enum class BaseLayer(val tileSource: ITileSource) {
    MAPNIK(MapnikTileSource),
    OPEN_TOPO(TileSourceFactory.OpenTopo),
    TUHU(TuHuTileSource),
    GOOGLE_SATELLITE(GoogleSateliteTileSource),
    MERRETEKERJEK(MerreTekerjekTileSource),
}
