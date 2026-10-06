package hu.mostoha.mobile.android.huki.model.domain

enum class RoutingMode {

    /**
     * GraphHopper first, HuKi-Routing only near or after the daily GraphHopper limit.
     */
    AUTO,

    GRAPHHOPPER_ONLY,

    HUKI_ROUTING_ONLY,

}
