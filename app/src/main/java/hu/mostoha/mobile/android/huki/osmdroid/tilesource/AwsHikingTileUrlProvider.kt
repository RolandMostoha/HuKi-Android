package hu.mostoha.mobile.android.huki.osmdroid.tilesource

import com.amplifyframework.core.Amplify
import hu.mostoha.mobile.android.huki.osmdroid.CounterProvider
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class AwsHikingTileUrlProvider @Inject constructor() : HikingTileUrlProvider {

    override fun getHikingTileUrl(storageKey: String): String {
        var url = ""

        val latch = CountDownLatch(1)
        Amplify.Storage.getUrl(
            storageKey,
            { storageGetUrlResult ->
                url = storageGetUrlResult.url.toString()

                Timber.d("Created url for S3 tile: $url")

                CounterProvider.tileDownloadRequestCounter++

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
