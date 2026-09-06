plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.detekt)
}

val detektVersion = libs.versions.detekt.get()

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

allprojects {
    apply(plugin = "dev.detekt")

    detekt {
        config.setFrom("${rootProject.projectDir}/tools/quality/HuKi-detekt.yml")
        allRules = true
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    dependencies {
        detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:$detektVersion")
    }
}
