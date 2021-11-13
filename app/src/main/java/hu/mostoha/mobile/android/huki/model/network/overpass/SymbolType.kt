package hu.mostoha.mobile.android.huki.model.network.overpass

import androidx.annotation.DrawableRes
import com.squareup.moshi.Json
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.IconEnum

enum class SymbolType : IconEnum {

    UNHANDLED {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_empty
    },

    @Json(name = "k")
    K {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_k
    },

    @Json(name = "k+")
    KP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kp
    },

    @Json(name = "k3")
    K3 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_k3
    },

    @Json(name = "k4")
    K4 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_k4
    },

    @Json(name = "kq")
    KQ {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kq
    },

    @Json(name = "kb")
    KB {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kb
    },

    @Json(name = "kl")
    KL {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kl
    },

    @Json(name = "kc")
    KC {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kc
    },

    @Json(name = "kt")
    KT {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kt
    },

    @Json(name = "keml")
    KEML {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_keml
    },

    @Json(name = "ktmp")
    KTMP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_ktmp
    },

    @Json(name = "kx")
    KX {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kx
    },

    @Json(name = "kii")
    KII {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kii
    },

    @Json(name = "kpec")
    KPEC {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_kpec
    },

    @Json(name = "p")
    P {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_p
    },

    @Json(name = "p+")
    PP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pp
    },

    @Json(name = "p3")
    P3 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_p3
    },

    @Json(name = "p4")
    P4 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_p4
    },

    @Json(name = "pq")
    PQ {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pq
    },

    @Json(name = "pb")
    PB {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pb
    },

    @Json(name = "pl")
    PL {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pl
    },

    @Json(name = "pc")
    PC {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pc
    },

    @Json(name = "pt")
    PT {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pt
    },

    @Json(name = "peml")
    PEML {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_peml
    },

    @Json(name = "ptmp")
    PTMP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_ptmp
    },

    @Json(name = "px")
    PX {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_px
    },

    @Json(name = "pii")
    PII {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pii
    },

    @Json(name = "z")
    Z {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_z
    },

    @Json(name = "z+")
    ZP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zp
    },

    @Json(name = "z3")
    Z3 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_z3
    },

    @Json(name = "z4")
    Z4 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_z4
    },

    @Json(name = "zq")
    ZQ {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zq
    },

    @Json(name = "zb")
    ZB {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zb
    },

    @Json(name = "zl")
    ZL {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zl
    },

    @Json(name = "zc")
    ZC {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zc
    },

    @Json(name = "zt")
    ZT {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zt
    },

    @Json(name = "zeml")
    ZEML {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zeml
    },

    @Json(name = "ztmp")
    ZTMP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_ztmp
    },

    @Json(name = "zx")
    ZX {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zx
    },

    @Json(name = "zii")
    ZII {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zii
    },

    @Json(name = "s")
    S {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_s
    },

    @Json(name = "s+")
    SP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sp
    },

    @Json(name = "s3")
    S3 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_s3
    },

    @Json(name = "s4")
    S4 {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_s4
    },

    @Json(name = "sq")
    SQ {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sq
    },

    @Json(name = "sb")
    SB {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sb
    },

    @Json(name = "sl")
    SL {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sl
    },

    @Json(name = "sc")
    SC {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sc
    },

    @Json(name = "st")
    ST {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_st
    },

    @Json(name = "seml")
    SEML {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_seml
    },

    @Json(name = "stmp")
    STMP {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_stmp
    },

    @Json(name = "sx")
    SX {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sx
    },

    @Json(name = "sii")
    SII {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sii
    },

    @Json(name = "t")
    T {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_t
    },

    @Json(name = "lm")
    LM {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_lm
    },

    @Json(name = "km")
    KM {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_km
    },

    @Json(name = "sm")
    SM {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_sm
    },

    @Json(name = "zm")
    ZM {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_zm
    },

    @Json(name = "pm")
    PM {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_pm
    },

    @Json(name = "smz")
    SMZ {
        @DrawableRes
        override fun getIconRes(): Int = R.drawable.ic_symbol_smz
    }

}
