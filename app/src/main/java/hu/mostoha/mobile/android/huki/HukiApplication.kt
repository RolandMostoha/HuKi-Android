package hu.mostoha.mobile.android.huki

import android.app.Application
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthFetchSessionOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import hu.mostoha.mobile.android.huki.logger.ExceptionLogger
import hu.mostoha.mobile.android.huki.osmdroid.OsmConfiguration
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class HukiApplication : Application() {

    @Inject
    lateinit var osmConfiguration: OsmConfiguration

    @Inject
    lateinit var exceptionLogger: ExceptionLogger

    private val isRelease = !BuildConfig.DEBUG

    override fun onCreate() {
        super.onCreate()

        osmConfiguration.init()

        initAnalytics()
        initAmplify()
        initTimber()
    }

    private fun initAnalytics() {
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(isRelease)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isRelease)
    }

    private fun initAmplify() {
        try {
            val config = AmplifyConfiguration
                .builder(applicationContext)
                .devMenuEnabled(false)
                .build()
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(config, applicationContext)

            forceRefreshToken()
        } catch (exception: AmplifyException) {
            Timber.e(exception)

            exceptionLogger.recordException(exception)
        }
    }

    private fun forceRefreshToken() {
        Amplify.Auth.fetchAuthSession(
            AuthFetchSessionOptions.builder()
                .forceRefresh(true)
                .build(),
            { _ -> },
            { authException ->
                Timber.e(authException, "Amplify.Auth failed to fetch auth session")

                exceptionLogger.recordException(authException)
            }
        )
    }

    private fun initTimber() {
        if (!isRelease) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
