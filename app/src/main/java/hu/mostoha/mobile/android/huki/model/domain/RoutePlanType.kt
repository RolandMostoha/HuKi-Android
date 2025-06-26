package hu.mostoha.mobile.android.huki.model.domain

sealed class RoutePlanType {

    data object Hike : RoutePlanType()

    data object Foot : RoutePlanType()

    data object Bike : RoutePlanType()

    data class RoundTrip(val distanceM: Int = 0) : RoutePlanType()

}
