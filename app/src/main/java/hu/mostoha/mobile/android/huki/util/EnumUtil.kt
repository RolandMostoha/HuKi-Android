package hu.mostoha.mobile.android.huki.util

object EnumUtil {

    /**
     * Returns the enum value for a String from the enum values related to the given [Enum], or null if there
     * are no corresponding values for the given string.
     *
     * @param value the enum value to be checked.
     * @return the enum for the given String value, or null if there are no associated values found.
     */
    fun <T : Enum<T>> List<T>.valueOf(value: String?): T? {
        return this.firstOrNull { type ->
            type.name.equals(value, ignoreCase = true)
        }
    }

}
