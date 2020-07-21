package hu.mostoha.mobile.android.turistautak.extensions

fun String.fixQueryErrors(): String {
    val settingsPart = this.split(";").first()
    return this.replace(settingsPart, settingsPart.replace("\"", ""))
}