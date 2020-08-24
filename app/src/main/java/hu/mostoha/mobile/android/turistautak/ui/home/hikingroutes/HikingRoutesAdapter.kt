package hu.mostoha.mobile.android.turistautak.ui.home.hikingroutes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import hu.mostoha.mobile.android.turistautak.extensions.setDrawableStart
import hu.mostoha.mobile.android.turistautak.model.ui.HikingRouteUiModel
import kotlinx.android.synthetic.main.item_home_hiking_routes.view.*
import kotlinx.android.synthetic.main.item_home_hiking_routes_header.view.*

class HikingRoutesAdapter(
    val onClick: (HikingRouteUiModel) -> Unit
) : ListAdapter<HikingRoutesItem, RecyclerView.ViewHolder>(HikingRoutesComparator) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HikingRoutesItem.Header -> TYPE_HEADER
            is HikingRoutesItem.Item -> TYPE_ITEM
            else -> throw IllegalArgumentException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                ViewHolderHeader(parent.context.inflateLayout(R.layout.item_home_hiking_routes_header, parent))
            }
            TYPE_ITEM -> {
                ViewHolderItem(parent.context.inflateLayout(R.layout.item_home_hiking_routes, parent))
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

    class ViewHolderHeader(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(text: String) {
            view.hikingRoutesHeaderText.text = text
        }
    }

    inner class ViewHolderItem(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(hikingRouteUiModel: HikingRouteUiModel) {
            view.hikingRoutesItemText.text = hikingRouteUiModel.name
            view.hikingRoutesItemText.setDrawableStart(hikingRouteUiModel.symbolIcon)
            view.hikingRoutesItemText.setOnClickListener {
                onClick.invoke(hikingRouteUiModel)
            }
        }
    }

}

sealed class HikingRoutesItem {
    data class Header(val text: String) : HikingRoutesItem()
    data class Item(val hikingRouteUiModel: HikingRouteUiModel) : HikingRoutesItem()
}

object HikingRoutesComparator : DiffUtil.ItemCallback<HikingRoutesItem>() {

    override fun areItemsTheSame(oldItem: HikingRoutesItem, newItem: HikingRoutesItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: HikingRoutesItem, newItem: HikingRoutesItem): Boolean {
        return when {
            oldItem is HikingRoutesItem.Header && newItem is HikingRoutesItem.Header -> {
                oldItem.text == newItem.text
            }
            oldItem is HikingRoutesItem.Item && newItem is HikingRoutesItem.Item -> {
                oldItem.hikingRouteUiModel == newItem.hikingRouteUiModel
            }
            else -> false
        }
    }

}
