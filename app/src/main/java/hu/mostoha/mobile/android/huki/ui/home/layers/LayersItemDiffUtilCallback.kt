package hu.mostoha.mobile.android.huki.ui.home.layers

import androidx.recyclerview.widget.DiffUtil

object LayersItemDiffUtilCallback : DiffUtil.ItemCallback<LayersAdapterItem>() {

    override fun areItemsTheSame(oldItem: LayersAdapterItem, newItem: LayersAdapterItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: LayersAdapterItem, newItem: LayersAdapterItem): Boolean {
        return when {
            oldItem is LayersAdapterItem.Header && newItem is LayersAdapterItem.Header -> {
                oldItem.titleRes == newItem.titleRes
            }
            oldItem is LayersAdapterItem.Layer && newItem is LayersAdapterItem.Layer -> {
                oldItem == newItem
            }
            else -> false
        }
    }

}
