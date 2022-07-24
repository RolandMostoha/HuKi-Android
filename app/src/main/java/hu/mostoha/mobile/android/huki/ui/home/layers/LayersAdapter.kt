package hu.mostoha.mobile.android.huki.ui.home.layers

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ItemLayersHeaderBinding
import hu.mostoha.mobile.android.huki.databinding.ItemLayersLayerBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.visibleOrGone

class LayersAdapter(
    val onLayerClick: (LayersAdapterItem.Layer) -> Unit
) : ListAdapter<LayersAdapterItem, RecyclerView.ViewHolder>(LayersItemDiffUtilCallback) {

    companion object {
        const val TYPE_LAYER_HEADER = 0
        const val TYPE_LAYER_ITEM = 1
        const val SPAN_COUNT_MAX = 2
        const val SPAN_COUNT_LAYER_HEADER = 2
        const val SPAN_COUNT_LAYER_BASE = 1
        const val SPAN_COUNT_LAYER_HIKING = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LAYER_HEADER -> {
                ViewHolderHeader(ItemLayersHeaderBinding.inflate(parent.context.inflater, parent, false))
            }
            TYPE_LAYER_ITEM -> {
                ViewHolderItem(ItemLayersLayerBinding.inflate(parent.context.inflater, parent, false))
            }
            else -> throw IllegalArgumentException("Not supported viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderHeader -> {
                holder.bind((getItem(position) as LayersAdapterItem.Header).titleRes)
            }
            is ViewHolderItem -> {
                holder.bind((getItem(position) as LayersAdapterItem.Layer))
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LayersAdapterItem.Header -> TYPE_LAYER_HEADER
            is LayersAdapterItem.Layer -> TYPE_LAYER_ITEM
        }
    }

    inner class ViewHolderHeader(private val binding: ItemLayersHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(@StringRes titleRes: Int) {
            binding.layersHeaderTitle.text = binding.root.context.getString(titleRes)
        }
    }

    inner class ViewHolderItem(private val binding: ItemLayersLayerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(layer: LayersAdapterItem.Layer) {
            with(binding) {
                itemLayersName.text = binding.root.context.getString(layer.titleRes)
                itemLayersImage.setImageResource(layer.drawableRes)
                itemLayersImageCard.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    if (layer.isSelected) {
                        R.color.colorPrimary
                    } else {
                        R.color.colorStroke
                    }
                )
                itemLayersSelectedImage.visibleOrGone(layer.isSelected)
                itemLayersImageCard.setOnClickListener {
                    onLayerClick.invoke(layer)
                }
            }
        }
    }

}
