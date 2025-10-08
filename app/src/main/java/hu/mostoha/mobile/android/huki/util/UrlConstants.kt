package hu.mostoha.mobile.android.huki.util

import hu.mostoha.mobile.android.huki.model.domain.HikeRecommendation

const val GOOGLE_MAPS_DIRECTIONS_URL = "https://www.google.com/maps/dir/?api=1&destination=%s,%s"

const val TERMESZETJARO_ZOOM_LEVEL = 11
const val TERMESZETJARO_SORT_PARAM = "&filter=sb-sortedBy-3"
val TERMESZETJARO_PLACE_URL =
    "${HikeRecommendation.TERMESZETJARO.baseUrl}/hu/tours/?cat=22729870#area=*$TERMESZETJARO_SORT_PARAM&zc=$TERMESZETJARO_ZOOM_LEVEL,%s"

const val KEKTURA_URL = "https://www.kektura.hu/"
const val KEKTURA_OKT_URL = "https://www.kektura.hu/okt-szakaszok"
const val KEKTURA_OKT_URL_TEMPLATE = "https://www.kektura.hu/okt-szakasz/%s"
const val KEKTURA_RPDDK_URL = "https://www.kektura.hu/rpddk-szakaszok"
const val KEKTURA_RPDDK_URL_TEMPLATE = "https://www.kektura.hu/rpddk-szakasz/%s"
const val KEKTURA_AKT_URL = "https://www.kektura.hu/ak-szakaszok"
const val KEKTURA_AKT_URL_TEMPLATE = "https://www.kektura.hu/ak-szakasz/%s"
