package hu.mostoha.mobile.android.huki.views

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.skydoves.powermenu.MenuBaseAdapter
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ListItemPopupMenuBinding
import hu.mostoha.mobile.android.huki.extensions.PopupMenuItem
import hu.mostoha.mobile.android.huki.extensions.inflater
import hu.mostoha.mobile.android.huki.extensions.setImageOrGone
import hu.mostoha.mobile.android.huki.extensions.setTextOrGone
import hu.mostoha.mobile.android.huki.util.color

class PopupMenuAdapter : MenuBaseAdapter<PopupMenuItem>() {

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup): View {
        var view = convertView
        val context = viewGroup.context
        val item = getItem(index) as PopupMenuItem

        if (view == null) {
            view = ListItemPopupMenuBinding.inflate(context.inflater, viewGroup, false).root
        }

        val container = view.findViewById<LinearLayout>(R.id.popupMenuContainer)
        val title = view.findViewById<TextView>(R.id.popupMenuTitle)
        val subTitle = view.findViewById<TextView>(R.id.popupMenuSubtitle)
        val icon = view.findViewById<ImageView>(R.id.popupMenuIcon)
        val endIcon = view.findViewById<ImageView>(R.id.popupMenuEndIcon)

        title.setTextOrGone(item.titleId)
        subTitle.setTextOrGone(item.subTitleId)
        icon.setImageOrGone(item.startIconId)
        endIcon.setImageOrGone(item.endIconId)
        if (item.backgroundColor != null) {
            container.setBackgroundColor(item.backgroundColor.color(context))
        }

        return super.getView(index, view, viewGroup)
    }

}
