package hu.mostoha.mobile.android.huki.osmdroid

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.TileZoomRange
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.AwsHikingTileSource
import hu.mostoha.mobile.android.huki.osmdroid.tilesource.HikingTileUrlProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class AwsHikingTileSourceTest {

    private val tileUrlProvider = mockk<HikingTileUrlProvider>()

    @Test
    fun `Given empty tile zoom range list, when getTileURLString, then get hiking tile url is invoked with proper storage key`() {
        val tileZoomRanges = emptyList<TileZoomRange>()
        val tileSource = AwsHikingTileSource(tileUrlProvider, tileZoomRanges)
        val tileProviderUrl = "www.google.com"
        every { tileUrlProvider.getHikingTileUrl(any()) } returns tileProviderUrl

        val tileUrl = tileSource.getTileURLString(1)

        verify { tileUrlProvider.getHikingTileUrl("0/0/1.png") }
        assertThat(tileUrl).isEqualTo(tileProviderUrl)
    }

    @Test
    fun `Given coordinates within zoom range list, when getTileURLString, then get hiking tile url is invoked with proper storage key`() {
        val tileZoomRanges = listOf(TileZoomRange(0, 0, 1, 0, 1))
        val tileSource = AwsHikingTileSource(tileUrlProvider, tileZoomRanges)
        val tileProviderUrl = "www.google.com"
        every { tileUrlProvider.getHikingTileUrl(any()) } returns tileProviderUrl

        val tileUrl = tileSource.getTileURLString(1)

        verify { tileUrlProvider.getHikingTileUrl("0/0/1.png") }
        assertThat(tileUrl).isEqualTo(tileProviderUrl)
    }

    @Test
    fun `Given zoom out of range list, when getTileURLString, then empty string returns`() {
        val tileZoomRanges = listOf(TileZoomRange(1, 0, 1, 0, 1))
        val tileSource = AwsHikingTileSource(tileUrlProvider, tileZoomRanges)
        val tileProviderUrl = "www.google.com"
        every { tileUrlProvider.getHikingTileUrl(any()) } returns tileProviderUrl

        val tileUrl = tileSource.getTileURLString(1)

        verify(inverse = true) { tileUrlProvider.getHikingTileUrl(any()) }
        assertThat(tileUrl).isEmpty()
    }

    @Test
    fun `Given y out of range list, when getTileURLString, then empty string returns`() {
        val tileZoomRanges = listOf(TileZoomRange(1, 0, 1, 0, 1))
        val tileSource = AwsHikingTileSource(tileUrlProvider, tileZoomRanges)
        val tileProviderUrl = "www.google.com"
        every { tileUrlProvider.getHikingTileUrl(any()) } returns tileProviderUrl

        val tileUrl = tileSource.getTileURLString(2)

        verify(inverse = true) { tileUrlProvider.getHikingTileUrl(any()) }
        assertThat(tileUrl).isEmpty()
    }

}
