package hu.mostoha.mobile.android.huki.data

import hu.mostoha.mobile.android.huki.model.domain.Location
import hu.mostoha.mobile.android.huki.model.domain.OktRoute
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

const val OKT_ID_FULL_ROUTE = "OKT"

val LOCAL_OKT_ROUTES = listOf(
    OktRoute(
        id = OKT_ID_FULL_ROUTE,
        name = "Írott-kő - Hollóháza",
        distanceKm = 1172.5,
        incline = 31455,
        decline = 32035,
        travelTime = 344.hours.plus(40.minutes),
        start = Location(latitude = 47.35275964, longitude = 16.433750795, altitude = 880.1462),
        end = Location(latitude = 48.541987393, longitude = 21.413286913, altitude = 316.9324)
    ),
    OktRoute(
        id = "OKT-01",
        name = "Írott-kő - Sárvár",
        distanceKm = 72.5,
        incline = 570,
        decline = 1290,
        travelTime = 18.hours.plus(50.minutes),
        start = Location(latitude = 47.35275964, longitude = 16.433750795, altitude = 880.1462),
        end = Location(latitude = 47.245569866, longitude = 16.940811899, altitude = 154.3261)
    ),
    OktRoute(
        id = "OKT-02",
        name = "Sárvár - Sümeg",
        distanceKm = 72.4,
        incline = 450,
        decline = 455,
        travelTime = 18.hours.plus(40.minutes),
        start = Location(latitude = 47.245569866, longitude = 16.940811899, altitude = 154.3261),
        end = Location(latitude = 46.981292481, longitude = 17.271265176, altitude = 167.3507)
    ),
    OktRoute(
        id = "OKT-03",
        name = "Sümeg - Keszthely",
        distanceKm = 45.3,
        incline = 820,
        decline = 890,
        travelTime = 12.hours.plus(40.minutes),
        start = Location(latitude = 46.981292481, longitude = 17.271265176, altitude = 167.3507),
        end = Location(latitude = 46.759963612, longitude = 17.249656386, altitude = 105.3006)
    ),
    OktRoute(
        id = "OKT-04",
        name = "Keszthely - Tapolca",
        distanceKm = 29.4,
        incline = 560,
        decline = 545,
        travelTime = 8.hours.plus(20.minutes),
        start = Location(latitude = 46.759963612, longitude = 17.249656386, altitude = 105.3006),
        end = Location(latitude = 46.877354824, longitude = 17.4290248, altitude = 120.3302)
    ),
    OktRoute(
        id = "OKT-05",
        name = "Tapolca - Badacsonytördemic",
        distanceKm = 17.2,
        incline = 380,
        decline = 385,
        travelTime = 5.hours.plus(0.minutes),
        start = Location(latitude = 46.877354824, longitude = 17.4290248, altitude = 120.3302),
        end = Location(latitude = 46.808472494, longitude = 17.469138285, altitude = 110.4971)
    ),
    OktRoute(
        id = "OKT-06",
        name = "Badacsonytördemic - Nagyvázsony",
        distanceKm = 48.2,
        incline = 1540,
        decline = 1400,
        travelTime = 14.hours.plus(30.minutes),
        start = Location(latitude = 46.808472494, longitude = 17.469138285, altitude = 110.4971),
        end = Location(latitude = 46.984919493, longitude = 17.694656054, altitude = 255.6064)
    ),
    OktRoute(
        id = "OKT-07",
        name = "Nagyvázsony - Városlőd",
        distanceKm = 22.6,
        incline = 470,
        decline = 440,
        travelTime = 6.hours.plus(30.minutes),
        start = Location(latitude = 46.984919493, longitude = 17.694656054, altitude = 255.6064),
        end = Location(latitude = 47.134760357, longitude = 17.631173723, altitude = 285.688)
    ),
    OktRoute(
        id = "OKT-08",
        name = "Városlőd - Zirc",
        distanceKm = 41.4,
        incline = 1085,
        decline = 990,
        travelTime = 12.hours.plus(10.minutes),
        start = Location(latitude = 47.134760357, longitude = 17.631173723, altitude = 285.688),
        end = Location(latitude = 47.264708926, longitude = 17.87862525, altitude = 382.1969)
    ),
    OktRoute(
        id = "OKT-09",
        name = "Zirc - Bodajk",
        distanceKm = 58.6,
        incline = 1235,
        decline = 1465,
        travelTime = 16.hours.plus(50.minutes),
        start = Location(latitude = 47.264708926, longitude = 17.87862525, altitude = 382.1969),
        end = Location(latitude = 47.32453117, longitude = 18.231927305, altitude = 151.2713)
    ),
    OktRoute(
        id = "OKT-10",
        name = "Bodajk - Szárliget",
        distanceKm = 56.3,
        incline = 1365,
        decline = 1290,
        travelTime = 16.hours.plus(20.minutes),
        start = Location(latitude = 47.32453117, longitude = 18.231927305, altitude = 151.2713),
        end = Location(latitude = 47.518552321, longitude = 18.49622075, altitude = 228.9385)
    ),
    OktRoute(
        id = "OKT-11",
        name = "Szárliget - Dorog",
        distanceKm = 67.6,
        incline = 2130,
        decline = 2235,
        travelTime = 20.hours.plus(20.minutes),
        start = Location(latitude = 47.518552321, longitude = 18.49622075, altitude = 228.9385),
        end = Location(latitude = 47.722834587, longitude = 18.732393169, altitude = 128.5994)
    ),
    OktRoute(
        id = "OKT-12",
        name = "Dorog - Piliscsaba",
        distanceKm = 18.6,
        incline = 530,
        decline = 420,
        travelTime = 5.hours.plus(30.minutes),
        start = Location(latitude = 47.723625871, longitude = 18.733692271, altitude = 127.5915),
        end = Location(latitude = 47.639424362, longitude = 18.828363335, altitude = 233.5294)
    ),
    OktRoute(
        id = "OKT-13",
        name = "Piliscsaba - Hűvösvölgy",
        distanceKm = 22.3,
        incline = 580,
        decline = 585,
        travelTime = 6.hours.plus(30.minutes),
        start = Location(latitude = 47.639424362, longitude = 18.828363335, altitude = 233.5294),
        end = Location(latitude = 47.541987665, longitude = 18.963891521, altitude = 225.411)
    ),
    OktRoute(
        id = "OKT-14",
        name = "Hűvösvölgy - Rozália téglagyár",
        distanceKm = 14.1,
        incline = 525,
        decline = 625,
        travelTime = 4.hours.plus(20.minutes),
        start = Location(latitude = 47.541987665, longitude = 18.963891521, altitude = 225.411),
        end = Location(latitude = 47.585046834, longitude = 18.989913034, altitude = 126.8612)
    ),
    OktRoute(
        id = "OKT-15",
        name = "Rozália téglagyár - Dobogókő",
        distanceKm = 22.8,
        incline = 1060,
        decline = 490,
        travelTime = 7.hours.plus(30.minutes),
        start = Location(latitude = 47.585046834, longitude = 18.989913034, altitude = 126.8612),
        end = Location(latitude = 47.719535433, longitude = 18.898673855, altitude = 698.1156)
    ),
    OktRoute(
        id = "OKT-16",
        name = "Dobogókő - Visegrád",
        distanceKm = 24.7,
        incline = 610,
        decline = 1205,
        travelTime = 7.hours.plus(10.minutes),
        start = Location(latitude = 47.719535433, longitude = 18.898673855, altitude = 698.1156),
        end = Location(latitude = 47.786831991, longitude = 18.967199298, altitude = 104.5608)
    ),
    OktRoute(
        id = "OKT-17",
        name = "Nagymaros - Nógrád",
        distanceKm = 41.0,
        incline = 1745,
        decline = 1635,
        travelTime = 13.hours.plus(0.minutes),
        start = Location(latitude = 47.789152523, longitude = 18.961532517, altitude = 104.3989),
        end = Location(latitude = 47.902609745, longitude = 19.045920016, altitude = 216.8286)
    ),
    OktRoute(
        id = "OKT-18",
        name = "Nógrád - Becske",
        distanceKm = 59.9,
        incline = 1880,
        decline = 1880,
        travelTime = 17.hours.plus(55.minutes),
        start = Location(latitude = 47.902609745, longitude = 19.045920016, altitude = 216.8286),
        end = Location(latitude = 47.910188534, longitude = 19.376131611, altitude = 226.1763)
    ),
    OktRoute(
        id = "OKT-19",
        name = "Becske - Mátraverebély",
        distanceKm = 73.9,
        incline = 2360,
        decline = 2395,
        travelTime = 22.hours.plus(20.minutes),
        start = Location(latitude = 47.910188534, longitude = 19.376131611, altitude = 226.1763),
        end = Location(latitude = 47.968481549, longitude = 19.779375313, altitude = 191.7785)
    ),
    OktRoute(
        id = "OKT-20",
        name = "Mátraverebély - Mátraháza",
        distanceKm = 25.5,
        incline = 1295,
        decline = 785,
        travelTime = 8.hours.plus(25.minutes),
        start = Location(latitude = 47.968343258, longitude = 19.779531902, altitude = 192.4598),
        end = Location(latitude = 47.868359372, longitude = 19.9781289, altitude = 707.2808)
    ),
    OktRoute(
        id = "OKT-21",
        name = "Mátraháza - Sirok",
        distanceKm = 26.1,
        incline = 1015,
        decline = 1565,
        travelTime = 8.hours.plus(10.minutes),
        start = Location(latitude = 47.868359372, longitude = 19.9781289, altitude = 707.2808),
        end = Location(latitude = 47.931651821, longitude = 20.194516272, altitude = 158.5833)
    ),
    OktRoute(
        id = "OKT-22",
        name = "Sirok - Szarvaskő",
        distanceKm = 18.0,
        incline = 585,
        decline = 520,
        travelTime = 5.hours.plus(30.minutes),
        start = Location(latitude = 47.931651821, longitude = 20.194516272, altitude = 158.5833),
        end = Location(latitude = 47.988100759, longitude = 20.331000562, altitude = 222.508)
    ),
    OktRoute(
        id = "OKT-23",
        name = "Szarvaskő - Putnok",
        distanceKm = 62.6,
        incline = 2255,
        decline = 2315,
        travelTime = 19.hours.plus(30.minutes),
        start = Location(latitude = 47.988100759, longitude = 20.331000562, altitude = 222.508),
        end = Location(latitude = 48.287092978, longitude = 20.437302787, altitude = 150.2519)
    ),
    OktRoute(
        id = "OKT-24",
        name = "Putnok - Bódvaszilas",
        distanceKm = 63.9,
        incline = 1755,
        decline = 1740,
        travelTime = 18.hours.plus(50.minutes),
        start = Location(latitude = 48.287092978, longitude = 20.437302787, altitude = 150.2519),
        end = Location(latitude = 48.537089852, longitude = 20.733679305, altitude = 165.4704)
    ),
    OktRoute(
        id = "OKT-25",
        name = "Bódvaszilas - Boldogkőváralja",
        distanceKm = 68.4,
        incline = 1495,
        decline = 1490,
        travelTime = 19.hours.plus(40.minutes),
        start = Location(latitude = 48.537089852, longitude = 20.733679305, altitude = 165.4704),
        end = Location(latitude = 48.345205607, longitude = 21.208960245, altitude = 163.3493)
    ),
    OktRoute(
        id = "OKT-26",
        name = "Boldogkőváralja - Nagy-nyugodó",
        distanceKm = 54.2,
        incline = 1675,
        decline = 1475,
        travelTime = 16.hours.plus(20.minutes),
        start = Location(latitude = 48.345205607, longitude = 21.208960245, altitude = 163.3493),
        end = Location(latitude = 48.392190874, longitude = 21.623977589, altitude = 362.3364)
    ),
    OktRoute(
        id = "OKT-27",
        name = "Nagy-nyugodó - Hollóháza",
        distanceKm = 44.9,
        incline = 1485,
        decline = 1525,
        travelTime = 13.hours.plus(50.minutes),
        start = Location(latitude = 48.392190874, longitude = 21.623977589, altitude = 362.3364),
        end = Location(latitude = 48.541987393, longitude = 21.413286913, altitude = 316.9324)
    ),
)