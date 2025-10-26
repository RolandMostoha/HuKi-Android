package hu.mostoha.mobile.android.huki.model.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.data.AKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.data.LOCAL_AKT_ROUTES
import hu.mostoha.mobile.android.huki.data.LOCAL_OKT_ROUTES
import hu.mostoha.mobile.android.huki.data.LOCAL_RPDDK_ROUTES
import hu.mostoha.mobile.android.huki.data.OKT_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.data.RPDDK_ID_FULL_ROUTE
import hu.mostoha.mobile.android.huki.util.KEKTURA_AKT_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_AKT_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_OKT_URL_TEMPLATE
import hu.mostoha.mobile.android.huki.util.KEKTURA_RPDDK_URL
import hu.mostoha.mobile.android.huki.util.KEKTURA_RPDDK_URL_TEMPLATE

enum class OktType(
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    @DrawableRes val icon: Int,
    val oktRouteList: List<OktRoute>,
    val fullRouteId: String,
    val stampTag: String,
    val baseUrl: String,
    val sectionTemplateUrl: String,
) {

    /**
     * Országos Kéktúra
     */
    OKT(
        title = R.string.okt_okt_title,
        subTitle = R.string.okt_okt_subtitle,
        icon = R.drawable.ic_okt_okt,
        oktRouteList = LOCAL_OKT_ROUTES,
        fullRouteId = OKT_ID_FULL_ROUTE,
        stampTag = "OKTPH",
        baseUrl = KEKTURA_OKT_URL,
        sectionTemplateUrl = KEKTURA_OKT_URL_TEMPLATE,
    ),

    /**
     * Rockenbauer Pál Dél-dunántúli Kéktúra
     */
    RPDDK(
        title = R.string.okt_rpddk_title,
        subTitle = R.string.okt_rpddk_subtitle,
        icon = R.drawable.ic_okt_rpddk,
        oktRouteList = LOCAL_RPDDK_ROUTES,
        fullRouteId = RPDDK_ID_FULL_ROUTE,
        stampTag = "DDKPH",
        baseUrl = KEKTURA_RPDDK_URL,
        sectionTemplateUrl = KEKTURA_RPDDK_URL_TEMPLATE,
    ),

    /**
     * Alföldi Kéktúra
     */
    AKT(
        title = R.string.okt_akt_title,
        subTitle = R.string.okt_akt_subtitle,
        icon = R.drawable.ic_okt_akt,
        oktRouteList = LOCAL_AKT_ROUTES,
        fullRouteId = AKT_ID_FULL_ROUTE,
        stampTag = "AKPH",
        baseUrl = KEKTURA_AKT_URL,
        sectionTemplateUrl = KEKTURA_AKT_URL_TEMPLATE,
    )
}
