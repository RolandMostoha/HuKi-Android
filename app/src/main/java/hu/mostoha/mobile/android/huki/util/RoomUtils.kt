package hu.mostoha.mobile.android.huki.util

fun String.replaceVowelsWithWildcards(): String {
    return this
        .replace(Regex("[aáàâã]"), "?")
        .replace(Regex("[eéèëê]"), "?")
        .replace(Regex("[iíìî]"), "?")
        .replace(Regex("[oóòôõ]"), "?")
        .replace(Regex("[uúùüû]"), "?")
}
