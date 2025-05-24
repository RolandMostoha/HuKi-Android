rootProject.name = "HuKi-Android"

include(":app")
include(":osm-overpasser")
include(":test-data")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
