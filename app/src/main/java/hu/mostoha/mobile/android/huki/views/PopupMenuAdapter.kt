package hu.mostoha.mobile.android.huki.views

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.skydoves.powermenu.MenuBaseAdapter
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ListItemPopupMenuBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater

class PopupMenuAdapter : MenuBaseAdapter<PopupMenuItem>() {

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup): View {
        var view = convertView
        val context = viewGroup.context
        val item = getItem(index) as PopupMenuItem

        if (view == null) {
            view = ListItemPopupMenuBinding.inflate(context.inflater, viewGroup, false).root
        }

        view.findViewById<TextView>(R.id.popupMenuText).apply {
            text = context.getString(item.titleId)
            setCompoundDrawablesWithIntrinsicBounds(item.iconId, 0, 0, 0)
        }

        return super.getView(index, view, viewGroup)
    }

}
