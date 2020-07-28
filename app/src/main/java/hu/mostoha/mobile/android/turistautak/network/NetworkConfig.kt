package hu.mostoha.mobile.android.turistautak.network

import hu.supercluster.overpasser.library.output.OutputFormat

object NetworkConfig {
    const val BASE_URL_OVERPASS = "https://overpass-api.de"
    const val TIMEOUT_IN_SECONDS = 30

    val OUTPUT_FORMAT_OVERPASS = OutputFormat.JSON
}