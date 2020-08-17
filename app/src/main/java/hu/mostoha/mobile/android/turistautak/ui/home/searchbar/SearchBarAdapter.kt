package hu.mostoha.mobile.android.turistautak.ui.home.searchbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import hu.mostoha.mobile.android.turistautak.R
import hu.mostoha.mobile.android.turistautak.extensions.inflateLayout
import hu.mostoha.mobile.android.turistautak.extensions.setTextOrGone
import hu.mostoha.mobile.android.turistautak.model.ui.PlacePredictionUiModel
import kotlinx.android.synthetic.main.item_home_search_bar.view.*

class SearchBarAdapter(context: Context) : ArrayAdapter<PlacePredictionUiModel>(context, 0) {

    private lateinit var itemList: List<PlacePredictionUiModel>

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
        holder.secondaryText.setTextOrGone(searchResultItem.secondaryText)
        holder.iconImage.setImageResource(searchResultItem.iconRes)

        return view
    }

    fun submitList(results: List<PlacePredictionUiModel>) {
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
