package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import kotlinx.android.synthetic.main.item_home_search_bar.view.*

class SearchBarAdapter(context: Context) : ArrayAdapter<PlacesResultUiModel>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View
        if (convertView == null) {
            view = parent.context.inflateLayout(R.layout.item_home_search_bar, parent, false)
            holder = ViewHolder()
            holder.primaryText = view.searchBarResultPrimaryText
            holder.secondaryText = view.searchBarResultSecondaryText
            holder.iconImage = view.homeSearchBarResultIcon
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val searchResultItem = getItem(position)!!

        holder.primaryText.text = searchResultItem.primaryText
        holder.secondaryText.text = searchResultItem.secondaryText
        holder.iconImage.setImageResource(searchResultItem.iconRes)

        return view
    }

    inner class ViewHolder {
        lateinit var primaryText: TextView
        lateinit var secondaryText: TextView
        lateinit var iconImage: ImageView
    }

}

data class PlacesResultUiModel(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    @DrawableRes val iconRes: Int
) {
    override fun toString(): String {
        return primaryText
    }
}