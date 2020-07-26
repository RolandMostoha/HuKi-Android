package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.DrawableRes
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import hu.mostoha.mobile.android.turistautak.extensions.setDrawableStart
import kotlinx.android.synthetic.main.item_home_search_bar.view.*

class SearchBarAdapter(context: Context) : ArrayAdapter<SearchResultItem>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View
        if (convertView == null) {
            view = parent.context.inflateLayout(R.layout.item_home_search_bar, parent, false)
            holder = ViewHolder()
            holder.searchResultText = view.homeSearchBarText
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val searchResultItem = getItem(position)!!

        holder.searchResultText.text = searchResultItem.name
        holder.searchResultText.setDrawableStart(searchResultItem.symbolRes)

        return view
    }

    inner class ViewHolder {
        lateinit var searchResultText: TextView
    }

}

data class SearchResultItem(val name: String, @DrawableRes val symbolRes: Int) {
    override fun toString(): String {
        return name
    }
}