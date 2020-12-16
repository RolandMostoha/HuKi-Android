package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import hu.mostoha.mobile.android.turistautak.extensions.setDrawableTop
import hu.mostoha.mobile.android.turistautak.extensions.setTextOrGone
import hu.mostoha.mobile.android.turistautak.model.ui.PlaceUiModel
import kotlinx.android.synthetic.main.item_home_search_bar_info.view.*
import kotlinx.android.synthetic.main.item_home_search_bar_place.view.*

class SearchBarAdapter(context: Context) : ArrayAdapter<SearchBarItem>(context, 0) {

    private lateinit var itemList: List<SearchBarItem>

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val searchBarItem = getItem(position)!!

        val holder: ViewHolder
        val view: View

        when (searchBarItem) {
            is SearchBarItem.Place -> {
                if (convertView == null || convertView.tag == null) {
                    view = parent.context.inflateLayout(R.layout.item_home_search_bar_place, parent, false)
                    holder = ViewHolder()
                    holder.primaryText = view.searchBarResultPrimaryText
                    holder.secondaryText = view.searchBarResultSecondaryText
                    holder.iconImage = view.homeSearchBarResultIcon
                    view.tag = holder
                } else {
                    view = convertView
                    holder = convertView.tag as ViewHolder
                }

                holder.primaryText.text = searchBarItem.placeUiModel.primaryText
                holder.secondaryText.setTextOrGone(searchBarItem.placeUiModel.secondaryText)
                holder.iconImage.setImageResource(searchBarItem.placeUiModel.iconRes)
            }
            is SearchBarItem.Info -> {
                view = parent.context.inflateLayout(R.layout.item_home_search_bar_info, parent, false)
                view.searchBarInfoView.text = parent.context.getString(searchBarItem.messageRes)
                view.searchBarInfoView.setDrawableTop(searchBarItem.drawableRes)
            }
        }

        return view
    }

    fun submitList(results: List<SearchBarItem>) {
        itemList = results

        clear()
        addAll(itemList)
    }

    inner class ViewHolder {
        lateinit var primaryText: TextView
        lateinit var secondaryText: TextView
        lateinit var iconImage: ImageView
    }

}

sealed class SearchBarItem {
    data class Place(val placeUiModel: PlaceUiModel) : SearchBarItem()
    data class Info(@StringRes val messageRes: Int, @DrawableRes val drawableRes: Int) : SearchBarItem()
}
