package hu.mostoha.mobile.android.turistautak.ui.home.layers

import androidx.recyclerview.widget.DiffUtil

object LayersComparator : DiffUtil.ItemCallback<BaseLayer>() {

    override fun areItemsTheSame(oldItem: BaseLayer, newItem: BaseLayer): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BaseLayer, newItem: BaseLayer): Boolean {
        return oldItem == newItem
    }

}