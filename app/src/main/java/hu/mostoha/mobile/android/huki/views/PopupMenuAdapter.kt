package hu.mostoha.mobile.android.huki.views

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.skydoves.powermenu.MenuBaseAdapter
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ListItemPopupMenuBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setImageOrGone
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone

class PopupMenuAdapter : MenuBaseAdapter<PopupMenuItem>() {

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup): View {
        var view = convertView
        val context = viewGroup.context
        val item = getItem(index) as PopupMenuItem

        if (view == null) {
            view = ListItemPopupMenuBinding.inflate(context.inflater, viewGroup, false).root
        }

        val title = view.findViewById<TextView>(R.id.popupMenuTitle)
        val subTitle = view.findViewById<TextView>(R.id.popupMenuSubtitle)
        val icon = view.findViewById<ImageView>(R.id.popupMenuIcon)

        title.setTextOrGone(item.titleId)
        subTitle.setTextOrGone(item.subTitleId)
        icon.setImageOrGone(item.iconId)

        return super.getView(index, view, viewGroup)
    }

}
