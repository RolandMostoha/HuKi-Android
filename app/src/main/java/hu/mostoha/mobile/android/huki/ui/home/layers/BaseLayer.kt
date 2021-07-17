package hu.mostoha.mobile.android.huki.ui.home.layers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.R

enum class BaseLayer(@StringRes val titleRes: Int, @DrawableRes val drawableRes: Int) {
    MAPNIK(R.string.layers_mapnik_name, R.drawable.ic_layers_mapnik)
}
