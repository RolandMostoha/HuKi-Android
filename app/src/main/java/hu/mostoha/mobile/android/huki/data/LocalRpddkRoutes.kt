package hu.mostoha.mobile.android.huki.data

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val RPDDK_ID_FULL_ROUTE = "RPDDK"

@Suppress("MagicNumber")
val LOCAL_RPDDK_ROUTES = listOf(
    OktRoute(
        id = RPDDK_ID_FULL_ROUTE,
        name = "Írott-kő - Szekszárd",
        distanceKm = 541.5,
        incline = 9520,
        decline = 10295,
        travelTime = 151.hours,
        start = Location(latitude = 47.352977037, longitude = 16.434255815, altitude = 878.2693),
        end = Location(latitude = 46.34876654, longitude = 18.711954527, altitude = 89.3726),
        stampTagsRange = 1.0..50.0,
    ),
    OktRoute(
        id = "RPDDK-01",
        name = "Írott-kő - Egyházasrádóc",
        distanceKm = 50.0,
        incline = 355,
        decline = 1030,
        travelTime = 13.hours.plus(10.minutes),
        start = Location(latitude = 47.352977037, longitude = 16.434255815, altitude = 878.2693),
        end = Location(latitude = 47.086455801, longitude = 16.612579635, altitude = 203.8634),
        stampTagsRange = 1.0..5.0,
    ),
    OktRoute(
        id = "RPDDK-02",
        name = "Egyházasrádóc - Őriszentpéter",
        distanceKm = 66.0,
        incline = 650,
        decline = 635,
        travelTime = 17.hours.plus(30.minutes),
        start = Location(latitude = 47.086455801, longitude = 16.612579635, altitude = 203.8634),
        end = Location(latitude = 46.838755712, longitude = 16.420923278, altitude = 225.1731),
        stampTagsRange = 5.0..11.0,
    ),
    OktRoute(
        id = "RPDDK-03",
        name = "Őriszentpéter - Zalalövő",
        distanceKm = 54.6,
        incline = 670,
        decline = 710,
        travelTime = 14.hours.plus(40.minutes),
        start = Location(latitude = 46.838755712, longitude = 16.420923278, altitude = 225.1731),
        end = Location(latitude = 46.84275891, longitude = 16.584466368, altitude = 187.3539),
        stampTagsRange = 11.0..15.2,
    ),
    OktRoute(
        id = "RPDDK-04",
        name = "Zalalövő - Rádiháza",
        distanceKm = 41.1,
        incline = 800,
        decline = 810,
        travelTime = 11.hours.plus(35.minutes),
        start = Location(latitude = 46.84275891, longitude = 16.584466368, altitude = 187.3539),
        end = Location(latitude = 46.655084937, longitude = 16.766744578, altitude = 174.0919),
        stampTagsRange = 15.1..19.2,
    ),
    OktRoute(
        id = "RPDDK-05",
        name = "Rádiháza - Palin",
        distanceKm = 59.3,
        incline = 1525,
        decline = 1530,
        travelTime = 17.hours.plus(10.minutes),
        start = Location(latitude = 46.655084937, longitude = 16.766744578, altitude = 174.0919),
        end = Location(latitude = 46.498726761, longitude = 16.985371247, altitude = 157.8147),
        stampTagsRange = 19.0..24.0,
    ),
    OktRoute(
        id = "RPDDK-06",
        name = "Palin - Zalakomár",
        distanceKm = 31.5,
        incline = 530,
        decline = 570,
        travelTime = 8.hours.plus(40.minutes),
        start = Location(latitude = 46.498726761, longitude = 16.985371247, altitude = 157.8147),
        end = Location(latitude = 46.525651901, longitude = 17.178119307, altitude = 123.177),
        stampTagsRange = 24.0..27.0,
    ),
    OktRoute(
        id = "RPDDK-07",
        name = "Zalakomár - Kaposmérő",
        distanceKm = 63.4,
        incline = 305,
        decline = 285,
        travelTime = 16.hours.plus(20.minutes),
        start = Location(latitude = 46.525651901, longitude = 17.178119307, altitude = 123.177),
        end = Location(latitude = 46.354726789, longitude = 17.692608748, altitude = 140.6003),
        stampTagsRange = 27.0..31.0,
    ),
    OktRoute(
        id = "RPDDK-08",
        name = "Kaposmérő - Abaliget vá.",
        distanceKm = 67.9,
        incline = 1535,
        decline = 1500,
        travelTime = 19.hours.plus(40.minutes),
        start = Location(latitude = 46.354726789, longitude = 17.692608748, altitude = 140.6003),
        end = Location(latitude = 46.152808899, longitude = 18.075423045, altitude = 175.6336),
        stampTagsRange = 31.0..37.0,
    ),
    OktRoute(
        id = "RPDDK-09",
        name = "Abaliget vá. - Zobákpuszta",
        distanceKm = 38.1,
        incline = 1390,
        decline = 1200,
        travelTime = 12.hours.plus(0.minutes),
        start = Location(latitude = 46.152808899, longitude = 18.075423045, altitude = 175.6336),
        end = Location(latitude = 46.184879668, longitude = 18.31929285, altitude = 362.6882),
        stampTagsRange = 37.0..42.0,
    ),
    OktRoute(
        id = "RPDDK-10",
        name = "Zobákpuszta - Mórágy",
        distanceKm = 37.7,
        incline = 875,
        decline = 1110,
        travelTime = 10.hours.plus(40.minutes),
        start = Location(latitude = 46.184879668, longitude = 18.31929285, altitude = 362.6882),
        end = Location(latitude = 46.214731507, longitude = 18.64259904, altitude = 126.2473),
        stampTagsRange = 42.0..47.2,
    ),
    OktRoute(
        id = "RPDDK-11",
        name = "Mórágy - Szekszárd",
        distanceKm = 32.0,
        incline = 885,
        decline = 920,
        travelTime = 9.hours.plus(40.minutes),
        start = Location(latitude = 46.214731507, longitude = 18.64259904, altitude = 126.2473),
        end = Location(latitude = 46.34876654, longitude = 18.711954527, altitude = 89.3726),
        stampTagsRange = 47.1..50.0,
    ),
)
