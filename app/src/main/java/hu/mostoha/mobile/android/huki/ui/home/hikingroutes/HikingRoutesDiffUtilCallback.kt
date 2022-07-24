package hu.mostoha.mobile.android.huki.ui.home.hikingroutes

import androidx.recyclerview.widget.DiffUtil

object HikingRoutesDiffUtilCallback : DiffUtil.ItemCallback<HikingRoutesItem>() {

    override fun areItemsTheSame(oldItem: HikingRoutesItem, newItem: HikingRoutesItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: HikingRoutesItem, newItem: HikingRoutesItem): Boolean {
        return when {
            oldItem is HikingRoutesItem.Header && newItem is HikingRoutesItem.Header -> {
                oldItem.title == newItem.title
            }
            oldItem is HikingRoutesItem.Item && newItem is HikingRoutesItem.Item -> {
                oldItem.hikingRouteUiModel == newItem.hikingRouteUiModel
            }
            else -> false
        }
    }

}
