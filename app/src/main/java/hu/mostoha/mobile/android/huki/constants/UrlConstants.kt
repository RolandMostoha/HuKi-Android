package hu.mostoha.mobile.android.huki.constants

const val GOOGLE_MAPS_DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1&destination=%s,%s"

const val TERMESZETJARO_URL = "https://www.termeszetjaro.hu"

const val TERMESZETJARO_AREA_URL = "$TERMESZETJARO_URL/hu/tours/?cat=22729870#area=%s&wt=%s"

const val TERMESZETJARO_ZOOM_LEVEL = 11

const val TERMESZETJARO_SORT_PARAM = "&filter=sb-sortedBy-3"

const val TERMESZETJARO_PLACE_URL =
    "$TERMESZETJARO_URL/hu/tours/?cat=22729870#area=*$TERMESZETJARO_SORT_PARAM&zc=$TERMESZETJARO_ZOOM_LEVEL,%s"

const val KIRANDULASTIPPEK_URL = "https://kirandulastippek.hu"

const val KIRANDULASTIPPEK_QUERY_URL = "$KIRANDULASTIPPEK_URL/%s?tag=gyalogtura"
