import org.ajoberstar.grgit.Grgit
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id(libs.plugins.kotlin.android.get().pluginId)
    alias(libs.plugins.kotlin.ksp)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.grgit)
}

android {
    namespace = "hu.mostoha.mobile.android.huki"
    compileSdk = 35

    defaultConfig {
        applicationId = "hu.mostoha.mobile.android.huki"
        minSdk = 26
        targetSdk = 35

        val versions = getVersions()
        versionCode = versions["versionCode"]?.toInt()
        versionName = versions["versionName"]

        setProperty("archivesBaseName", "HuKi_${versionName}_$versionCode")

        buildConfigField("String", "GRAPHHOPPER_API_KEY", getApiKey("GRAPHHOPPER_API_KEY"))
        buildConfigField("String", "LOCATION_IQ_API_KEY", getApiKey("LOCATION_IQ_API_KEY"))

        testInstrumentationRunner = "hu.mostoha.mobile.android.huki.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            manifestPlaceholders += mapOf(
                "appNameHuki" to "@string/huki_app_name_debug",
                "appIcon" to "@mipmap/ic_launcher_debug",
                "analyticsDisabled" to true,
            )
            buildConfigField("Boolean", "CRASHLYTICS_ENABLED", "false")
        }
        getByName("release") {
            manifestPlaceholders += mapOf(
                "appNameHuki" to "@string/huki_app_name",
                "appIcon" to "@mipmap/ic_launcher",
                "analyticsDisabled" to false,
            )
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res", "src/main/res_symbols")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        packaging {
            jniLibs.useLegacyPackaging = true
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md"
            )
        }
    }

    lint {
        abortOnError = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":osm-overpasser"))

    // Kotlin
    implementation(libs.kotlinx.coroutines.core)

    // Design & UI
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.android.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.github.douglasjunior.simpleTooltip)
    implementation(libs.github.skydoves.powermenu)
    implementation(libs.airbnb.lottie)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.facebook.shimmer)
    implementation(libs.jpwasabeef.recyclerview.animators)

    // Hilt
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // KTX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.bundles.ktx.lifecycle) // Using a bundle

    // Preferences
    implementation(libs.androidx.datastore.preferences)

    // Google Play Services Location
    implementation(libs.google.play.services.location)

    // Google Play Billing
    implementation(libs.android.billing.ktx)

    // GPX
    implementation(libs.github.ticofab.gpx.parser)
    implementation(libs.codebutchery.gpx.lib)

    // OSM
    implementation(libs.osmdroid.android)

    // Network
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.moshi)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.squareup.moshi.kotlin)
    ksp(libs.squareup.moshi.kotlin.codegen)

    // AWS
    implementation(libs.bundles.amplify)
    coreLibraryDesugaring(libs.android.tools.desugar.jdk.libs)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Logging
    implementation(libs.jakewharton.timber)

    // Analytics + Crashlytics
    implementation(libs.bundles.firebase)

    // Licenses
    implementation(libs.github.marcoscgdev.licenser)

    // Unit tests
    testImplementation(project(":test-data"))
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    testImplementation(libs.mockk.core)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.cash.turbine)

    // Instrumentation tests
    androidTestImplementation(project(":test-data"))
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.test.espresso.contrib)
    androidTestImplementation(libs.google.truth)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.cash.turbine)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.google.dagger.hilt.compiler)
    androidTestUtil(libs.androidx.test.orchestrator)
}

fun getVersions(): Map<String, String> {
    val git = Grgit.open(mapOf("currentDir" to file(project.rootDir)))

    val versionName = git.describe().replace(Regex("-\\w+$"), "")
    val versionCode = git.log().size.toString()
    println("VersionName: $versionName \nVersionCode: $versionCode")

    git.close()
    return mapOf("versionName" to versionName, "versionCode" to versionCode)
}

fun getApiKey(key: String): String {
    val props = Properties()
    props.load(FileInputStream(rootProject.file("./local.properties")))
    return props[key] as String
}
