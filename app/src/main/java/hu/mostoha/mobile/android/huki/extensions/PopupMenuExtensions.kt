package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.views.PopupMenuAdapter

fun Context.showPopupMenu(anchorView: View, actionItems: List<PopupMenuActionItem>) {
    CustomPowerMenu.Builder(this, PopupMenuAdapter())
        .addItemList(actionItems.map { PopupMenuItem(it.popupMenuItem.titleId, it.popupMenuItem.iconId) })
        .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT)
        .setShowBackground(false)
        .setMenuRadius(resources.getDimensionPixelSize(R.dimen.default_corner_size_surface).toFloat())
        .setWidth(resources.getDimensionPixelSize(R.dimen.default_popup_menu_width))
        .setAutoDismiss(true)
        .setOnMenuItemClickListener(
            OnMenuItemClickListener<PopupMenuItem> { _, menuItem ->
                val actionItem = actionItems.first { it.popupMenuItem.titleId == menuItem.titleId }

                actionItem.onClick.invoke()
            }
        )
        .build()
        .showAsAnchorCenter(anchorView)
}

class PopupMenuItem(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int
)

data class PopupMenuActionItem(
    val popupMenuItem: PopupMenuItem,
    val onClick: () -> Unit
)
