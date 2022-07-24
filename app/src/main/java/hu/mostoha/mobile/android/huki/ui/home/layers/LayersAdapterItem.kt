package hu.mostoha.mobile.android.huki.ui.home.layers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.huki.model.domain.LayerType

sealed class LayersAdapterItem {

    data class Header(@StringRes val titleRes: Int) : LayersAdapterItem()

    data class Layer(
        val layerType: LayerType,
        @StringRes val titleRes: Int,
        @DrawableRes val drawableRes: Int,
        val isSelected: Boolean
    ) : LayersAdapterItem()

}
