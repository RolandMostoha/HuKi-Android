package hu.mostoha.mobile.android.turistautak.ui.home.layers

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.databinding.ItemLayersBaseBinding
import hu.mostoha.mobile.android.turistautak.extensions.inflater

class LayersAdapter(
    val onItemClick: (BaseLayer) -> Unit,
) : ListAdapter<BaseLayer, LayersAdapter.ViewHolderItem>(LayersComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {
        return ViewHolderItem(ItemLayersBaseBinding.inflate(parent.context.inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolderItem(private val binding: ItemLayersBaseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(layer: BaseLayer) {
            with(binding) {
                itemLayersName.text = binding.root.context.getString(layer.titleRes)
                itemLayersImage.setImageResource(layer.drawableRes)
                itemLayersImageCard.strokeColor = ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                itemLayersImageCard.setOnClickListener {
                    onItemClick.invoke(layer)
                }
            }
        }
    }

}
