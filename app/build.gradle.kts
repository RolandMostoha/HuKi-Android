import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val appVersions = getVersions()

base {
    archivesName = "HuKi_${appVersions.name}_${appVersions.code}"
}

android {
    namespace = "hu.mostoha.mobile.android.huki"
    compileSdk = 37

    defaultConfig {
        applicationId = "hu.mostoha.mobile.android.huki"
        minSdk = 26
        targetSdk = 37

        versionCode = appVersions.code
        versionName = appVersions.name

        val now = ZonedDateTime.now()
        val buildDate = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        buildConfigField("String", "GRAPHHOPPER_API_KEY", getApiKey("GRAPHHOPPER_API_KEY"))
        buildConfigField("String", "LOCATION_IQ_API_KEY", getApiKey("LOCATION_IQ_API_KEY"))
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")

        testInstrumentationRunner = "hu.mostoha.mobile.android.huki.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin.compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
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
                "analyticsDisabled" to true,
            )
            buildConfigField("Boolean", "CRASHLYTICS_ENABLED", "false")
            kotlin.compilerOptions.freeCompilerArgs.add("-Xdebug")
        }
        getByName("release") {
            manifestPlaceholders += mapOf(
                "appNameHuki" to "@string/huki_app_name",
                "analyticsDisabled" to false,
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            res.directories.addAll(listOf("src/main/res", "src/main/res_symbols"))
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
    implementation(libs.google.code.gson)
    ksp(libs.squareup.moshi.kotlin.codegen)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Logging
    implementation(libs.jakewharton.timber)

    // Analytics + Crashlytics
    implementation(libs.bundles.firebase)

    // Licenses
    implementation(libs.github.marcoscgdev.licenser)

    // Leak canary
    debugImplementation(libs.leak.canary)

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
    androidTestImplementation(libs.hamcrest)
    androidTestImplementation(libs.hamcrest.core)
    androidTestImplementation(libs.hamcrest.library)
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

data class Versions(val name: String, val code: Int)

fun getVersions(): Versions {
    val versionName = git("describe").replace(Regex("-\\w+$"), "")
    val versionCode = git("rev-list", "--count", "HEAD").toInt()
    println("VersionName: $versionName \nVersionCode: $versionCode")

    return Versions(versionName, versionCode)
}

fun git(vararg args: String): String {
    val result = providers.exec {
        workingDir = rootProject.projectDir
        commandLine("git", *args)
    }
    return result.standardOutput.asText.get().trim()
}

fun getApiKey(key: String): String {
    val localProperties = providers
        .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .get()
    val props = Properties().apply { load(localProperties.reader()) }
    return props[key] as String
}
