package hu.mostoha.mobile.android.huki

import android.app.Application
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import dagger.hilt.android.HiltAndroidApp
import hu.mostoha.mobile.android.huki.interactor.exception.ExceptionLogger
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HukiApplication : Application() {

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @Inject
    lateinit var exceptionLogger: ExceptionLogger

    override fun onCreate() {
        super.onCreate()

        osmConfiguration.init()

        initAmplify()
        initTimber()
    }

    private fun initAmplify() {
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)
        } catch (exception: AmplifyException) {
            Timber.e(exception)

            exceptionLogger.recordException(exception)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
