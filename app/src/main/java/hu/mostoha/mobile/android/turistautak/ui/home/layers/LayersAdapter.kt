package hu.mostoha.mobile.android.turistautak.ui.home.layers

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import kotlinx.android.synthetic.main.item_layers_base.view.*

class LayersAdapter(
    val onItemClick: (BaseLayer) -> Unit,
) : ListAdapter<BaseLayer, LayersAdapter.ViewHolderItem>(Comparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {
        return ViewHolderItem(parent.context.inflateLayout(R.layout.item_layers_base, parent))
    }

    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolderItem(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(layer: BaseLayer) {
            with(view) {
                itemLayersName.text = view.context.getString(layer.titleRes)
                itemLayersImage.setImageResource(layer.drawableRes)
                itemLayersImageCard.strokeColor = ContextCompat.getColor(view.context, R.color.colorPrimary)
                itemLayersImageCard.setOnClickListener {
                    onItemClick.invoke(layer)
                }
            }
        }
    }

}

object Comparator : DiffUtil.ItemCallback<BaseLayer>() {

    override fun areItemsTheSame(oldItem: BaseLayer, newItem: BaseLayer): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: BaseLayer, newItem: BaseLayer): Boolean {
        return oldItem == newItem
    }

}
