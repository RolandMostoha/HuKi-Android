package hu.mostoha.mobile.android.huki.ui.home.hikingroutes

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.huki.databinding.ItemHomeHikingRoutesBinding
import hu.mostoha.mobile.android.huki.databinding.ItemHomeHikingRoutesHeaderBinding
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setDrawableStart
import hu.mostoha.mobile.android.huki.model.ui.HikingRouteUiModel

class HikingRoutesAdapter(
    val onItemClick: (HikingRouteUiModel) -> Unit,
    val onCloseClick: () -> Unit
) : ListAdapter<HikingRoutesItem, RecyclerView.ViewHolder>(HikingRoutesComparator) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HikingRoutesItem.Header -> TYPE_HEADER
            is HikingRoutesItem.Item -> TYPE_ITEM
            else -> throw IllegalArgumentException("Not handled ItemViewType")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                ViewHolderHeader(
                    ItemHomeHikingRoutesHeaderBinding.inflate(parent.context.inflater, parent, false)
                )
            }
            TYPE_ITEM -> {
                ViewHolderItem(
                    ItemHomeHikingRoutesBinding.inflate(parent.context.inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderHeader -> {
                holder.bind((getItem(position) as HikingRoutesItem.Header).text)
            }
            is ViewHolderItem -> {
                holder.bind((getItem(position) as HikingRoutesItem.Item).hikingRouteUiModel)
            }
        }
    }

    inner class ViewHolderHeader(
        private val binding: ItemHomeHikingRoutesHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String) {
            with(binding) {
                hikingRoutesHeaderText.text = text
                hikingRoutesCloseButton.setOnClickListener {
                    onCloseClick.invoke()
                }
            }
        }
    }

    inner class ViewHolderItem(
        private val binding: ItemHomeHikingRoutesBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hikingRouteUiModel: HikingRouteUiModel) {
            with(binding) {
                hikingRoutesItemText.text = hikingRouteUiModel.name
                hikingRoutesItemText.setDrawableStart(hikingRouteUiModel.symbolIcon)
                hikingRoutesItemText.setOnClickListener {
                    onItemClick.invoke(hikingRouteUiModel)
                }
            }
        }
    }

}