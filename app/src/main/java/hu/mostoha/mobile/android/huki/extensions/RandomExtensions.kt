package hu.mostoha.mobile.android.huki.extensions

private const val RANDOM_DIGIT_MIN = 0
private const val RANDOM_DIGIT_MAX = 9

fun getRandomNumberText(length: Int): String {
    return (RANDOM_DIGIT_MIN..RANDOM_DIGIT_MAX)
        .shuffled()
        .take(length)
        .joinToString("")
}
