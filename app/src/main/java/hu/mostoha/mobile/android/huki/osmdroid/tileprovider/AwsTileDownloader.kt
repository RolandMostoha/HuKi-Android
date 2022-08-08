package hu.mostoha.mobile.android.huki.osmdroid.tileprovider

import android.graphics.drawable.Drawable
import android.text.TextUtils
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.CantContinueException
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.TileDownloader
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.util.Counters
import org.osmdroid.tileprovider.util.StreamUtils
import org.osmdroid.util.MapTileIndex
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

/**
 * [TileDownloader] implementation that saves an empty blob in the tile cache DB if HTTP status code was 404.
 * The code is the almost identical kotlin version of the original [TileDownloader] implementation.

 * Saving the empty blob is needed to avoid repeating requests for getting unavailable tiles.
 */
class AwsTileDownloader : TileDownloader() {

    @Suppress("LongMethod", "ReturnCount", "TooGenericExceptionCaught")
    override fun downloadTile(
        pMapTileIndex: Long,
        redirectCount: Int,
        targetUrl: String,
        pFilesystemCache: IFilesystemCache,
        pTileSource: OnlineTileSourceBase
    ): Drawable? {
        val configuration = Configuration.getInstance()

        val userAgent = if (pTileSource.tileSourcePolicy.normalizesUserAgent()) {
            configuration.normalizedUserAgent
        } else {
            configuration.userAgentValue
        }

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var urlConnection: HttpURLConnection? = null
        var byteStream: ByteArrayInputStream? = null
        var dataStream: ByteArrayOutputStream? = null

        val formattedTileIndex = MapTileIndex.toString(pMapTileIndex)

        try {
            if (configuration.isDebugMode) {
                Timber.d("Downloading MapTile from url: $targetUrl")
            }

            if (TextUtils.isEmpty(targetUrl)) {
                return null
            }

            urlConnection = URL(targetUrl).openConnection() as HttpURLConnection
            urlConnection.useCaches = true
            urlConnection.setRequestProperty(configuration.userAgentHttpHeader, userAgent)
            for ((key, value) in configuration.additionalHttpRequestProperties) {
                urlConnection.setRequestProperty(key, value)
            }
            urlConnection.connect()

            val expirationTime = pTileSource.tileSourcePolicy.computeExpirationTime(
                urlConnection,
                System.currentTimeMillis()
            )
            val responseCode = urlConnection.responseCode

            if (responseCode != HTTP_OK) {
                Timber.w(
                    "Problem downloading MapTile: $formattedTileIndex HTTP response: ${urlConnection.responseMessage}"
                )
                Counters.tileDownloadErrors++

                if (responseCode == HTTP_NOT_FOUND) {
                    // Saving the empty blob in DB
                    pFilesystemCache.saveFile(
                        pTileSource,
                        pMapTileIndex,
                        ByteArrayInputStream(ByteArray(0)),
                        expirationTime
                    )
                }

                inputStream = urlConnection.errorStream

                return null
            }

            inputStream = urlConnection.inputStream

            dataStream = ByteArrayOutputStream()
            outputStream = BufferedOutputStream(dataStream, StreamUtils.IO_BUFFER_SIZE)

            StreamUtils.copy(inputStream, outputStream)
            outputStream.flush()

            val data = dataStream.toByteArray()
            byteStream = ByteArrayInputStream(data)

            // Save the image data to the cache
            pFilesystemCache.saveFile(pTileSource, pMapTileIndex, byteStream, expirationTime)
            byteStream.reset()

            return pTileSource.getDrawable(byteStream)

        } catch (unknownHostException: UnknownHostException) {
            Timber.w("UnknownHostException downloading MapTile: $formattedTileIndex : $unknownHostException")
            Counters.tileDownloadErrors++
        } catch (lowMemoryException: BitmapTileSourceBase.LowMemoryException) {
            Counters.countOOM++
            Timber.w("LowMemoryException downloading MapTile: $formattedTileIndex : $lowMemoryException")
            throw CantContinueException(lowMemoryException)
        } catch (fileNotFoundException: FileNotFoundException) {
            Counters.tileDownloadErrors++
            Timber.w("Tile not found: $formattedTileIndex : $fileNotFoundException")
        } catch (ioException: IOException) {
            Counters.tileDownloadErrors++
            Timber.w("IOException downloading MapTile: $formattedTileIndex : $ioException")
        } catch (throwable: Throwable) {
            Counters.tileDownloadErrors++
            Timber.e("Error downloading MapTile: $formattedTileIndex", throwable)
        } finally {
            StreamUtils.closeStream(inputStream)
            StreamUtils.closeStream(outputStream)
            StreamUtils.closeStream(byteStream)
            StreamUtils.closeStream(dataStream)

            urlConnection?.disconnect()
        }

        return null
    }

}
