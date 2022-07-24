package hu.mostoha.mobile.android.huki.osmdroid.tiles

import com.amplifyframework.core.Amplify
import timber.log.Timber
import java.util.concurrent.CountDownLatch

object AwsHikingTileUrlProvider : HikingTileUrlProvider {

    override fun getHikingTileUrl(storageKey: String): String {
        var url = ""

        val latch = CountDownLatch(1)
        Amplify.Storage.getUrl(
            storageKey,
            { storageGetUrlResult ->
                url = storageGetUrlResult.url.toString()

                Timber.d("Created url for S3 tile: $url")

                latch.countDown()
            },
            { storageException ->
                Timber.e(storageException)

                latch.countDown()
            }
        )
        latch.await()

        return url
    }

}
