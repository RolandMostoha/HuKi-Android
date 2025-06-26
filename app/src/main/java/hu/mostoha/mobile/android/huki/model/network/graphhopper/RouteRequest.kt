package hu.mostoha.mobile.android.huki.model.network.graphhopper

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteRequest(

    @Json(name = "profile")
    val profile: Profile,

    @Json(name = "points_encoded")
    val pointsEncoded: Boolean,

    @Json(name = "elevation")
    val elevation: Boolean,

    @Json(name = "instructions")
    val instructions: Boolean,

    @Json(name = "points")
    val points: List<List<Double>>,

    @Json(name = "custom_model")
    val customModel: CustomModel? = null,

    @Json(name = "ch.disable")
    val chDisabled: Boolean? = null,

    @Json(name = "algorithm")
    val algorithm: Algorithm? = null,

    /**
     * Default: 2
     * If algorithm=alternative_route, this parameter sets the number of maximum paths which should be
     * calculated. Increasing can lead to worse alternatives.
     */
    @Json(name = "alternative_route.max_paths")
    val alternativeMaxPaths: Int? = null,

    /**
     * Default: 1.4
     * If algorithm=alternative_route, this parameter sets the factor by which the alternatives routes can be
     * longer than the optimal route. Increasing can lead to worse alternatives.
     */
    @Json(name = "alternative_route.max_weight_factor")
    val alternativeMaxWeightFactor: Double? = null,

    /**
     * Default: 10000
     * If algorithm=round_trip, this parameter configures approximative length of the resulting round trip.
     */
    @Json(name = "round_trip.distance")
    val roundTripDistance: Int? = null,

    /**
     * If algorithm=round_trip, this sets the random seed. Change this to get a different tour for each value.
     */
    @Json(name = "round_trip.seed")
    val roundTripSeed: Int? = null,

    /**
     * Favour a heading direction for a certain point. Specify either one heading for the start point or
     * as many as there are points. In this case headings are associated by their order to the specific points.
     * Headings are given as north based clockwise angle between 0 and 360 degree.
     * This parameter also influences the tour generated with algorithm=round_trip and forces the initial
     * direction. Requires ch.disable=true.
     */
    @Json(name = "heading")
    val heading: List<Int>? = null,

    /**
     * Penalty for omitting a specified heading.
     * The penalty corresponds to the accepted time delay in seconds in comparison to the route without a heading.
     */
    @Json(name = "heading_penalty")
    val headingPenalty: Int? = null,

    /**
     * If true u-turns are avoided at via-points with regard to the heading_penalty.
     */
    @Json(name = "pass_through")
    val passThrough: Boolean? = null,

    )
