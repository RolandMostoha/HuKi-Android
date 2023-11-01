package hu.mostoha.mobile.android.huki.ui.formatter

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.toMessage
import org.osmdroid.util.GeoPoint
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object LocationFormatter {

    private val LOCATION_DECIMAL_FORMAT = DecimalFormat("0.0000").apply {
        decimalFormatSymbols = DecimalFormatSymbols().apply {
            decimalSeparator = '.'
        }
        roundingMode = RoundingMode.DOWN
    }

    fun formatString(location: Location): String {
        return LOCATION_DECIMAL_FORMAT.format(location.latitude)
            .plus(",")
            .plus(LOCATION_DECIMAL_FORMAT.format(location.longitude))
    }

    fun formatText(location: Location): Message.Text {
        return LOCATION_DECIMAL_FORMAT.format(location.latitude)
            .plus(",")
            .plus(LOCATION_DECIMAL_FORMAT.format(location.longitude))
            .toMessage()
    }

    fun formatText(geoPoint: GeoPoint): Message.Text {
        return formatText(geoPoint.toLocation())
    }

}
