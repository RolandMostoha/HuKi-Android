package hu.mostoha.mobile.android.huki.model.mapper

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.PlaceProfile
import hu.mostoha.mobile.android.huki.model.domain.toDomain
import hu.mostoha.mobile.android.huki.model.domain.toLocation
import hu.mostoha.mobile.android.huki.model.ui.LandscapeUiModel
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.PlaceArea
import hu.mostoha.mobile.android.huki.model.ui.PlaceAreaType
import hu.mostoha.mobile.android.huki.model.ui.PlaceUiModel
import hu.mostoha.mobile.android.huki.ui.formatter.LocationFormatter
import hu.mostoha.mobile.android.huki.util.areaDistance
import hu.mostoha.mobile.android.huki.util.areaDistanceMessage
import org.osmdroid.util.BoundingBox
import hu.mostoha.mobile.android.huki.model.domain.BoundingBox as DomainBoundingBox

object PlaceAreaMapper {

    private const val PLACE_CITY_THRESHOLD = 2_000
    private const val PLACE_COUNTRY_THRESHOLD = 50_000

    fun map(location: Location, boundingBox: DomainBoundingBox, placeProfile: PlaceProfile?): PlaceArea {
        val areaDistance = boundingBox.areaDistance()

        val address = when {
            placeProfile == null -> null
            areaDistance < PLACE_CITY_THRESHOLD -> placeProfile.address.name
            areaDistance in PLACE_CITY_THRESHOLD..PLACE_COUNTRY_THRESHOLD -> placeProfile.address.city
            else -> placeProfile.address.country
        }

        val addressMessage = if (address != null) {
            Message.Text(address)
        } else {
            LocationFormatter.formatText(location)
        }

        return PlaceArea(
            placeAreaType = PlaceAreaType.MAP_SEARCH,
            location = location,
            boundingBox = boundingBox,
            addressMessage = addressMessage,
            distanceMessage = boundingBox.areaDistanceMessage(),
            iconRes = R.drawable.ic_place_category_city
        )
    }

    fun map(landscapeUiModel: LandscapeUiModel, boundingBox: BoundingBox): PlaceArea {
        return PlaceArea(
            placeAreaType = PlaceAreaType.LANDSCAPE,
            location = landscapeUiModel.geoPoint.toLocation(),
            boundingBox = boundingBox.toDomain(),
            addressMessage = landscapeUiModel.name,
            distanceMessage = boundingBox.toDomain().areaDistanceMessage(),
            iconRes = landscapeUiModel.iconRes
        )
    }

    fun map(placeUiModel: PlaceUiModel, boundingBox: BoundingBox): PlaceArea {
        return PlaceArea(
            placeAreaType = PlaceAreaType.PLACE_DETAILS,
            location = placeUiModel.geoPoint.toLocation(),
            boundingBox = boundingBox.toDomain(),
            addressMessage = placeUiModel.primaryText,
            distanceMessage = boundingBox.toDomain().areaDistanceMessage(),
            iconRes = placeUiModel.iconRes
        )
    }

}
