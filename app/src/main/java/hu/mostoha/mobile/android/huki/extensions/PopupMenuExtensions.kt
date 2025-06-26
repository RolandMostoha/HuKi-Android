package hu.mostoha.mobile.android.huki.extensions

import android.content.Context
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.databinding.ViewPopupMenuHeaderBinding
import hu.mostoha.mobile.android.huki.model.ui.Message
import hu.mostoha.mobile.android.huki.model.ui.resolve
import hu.mostoha.mobile.android.huki.views.PopupMenuAdapter

fun Context.showPopupMenu(
    anchorView: View,
    actionItems: List<PopupMenuActionItem>,
    @DimenRes width: Int = R.dimen.default_popup_menu_width,
    showBackground: Boolean = true,
    showAtCenter: Boolean = false,
    headerTitle: Message? = null,
    footerView: View? = null,
) {
    val powerMenu = CustomPowerMenu.Builder(this, PopupMenuAdapter())
        .addItemList(
            actionItems.map { actionItem ->
                val menuItem = actionItem.popupMenuItem
                PopupMenuItem(
                    titleId = menuItem.titleId,
                    subTitleId = menuItem.subTitleId,
                    startIconId = menuItem.startIconId,
                    endIconId = menuItem.endIconId,
                    backgroundColor = menuItem.backgroundColor
                )
            }
        )
        .setAnimation(MenuAnimation.FADE)
        .setShowBackground(showBackground)
        .setMenuRadius(resources.getDimensionPixelSize(R.dimen.default_corner_size_popup_menu).toFloat())
        .setWidth(resources.getDimensionPixelSize(width))
        .setAutoDismiss(true)
        .setOnMenuItemClickListener(
            OnMenuItemClickListener<PopupMenuItem> { _, menuItem ->
                val actionItem = actionItems.first { it.popupMenuItem.titleId == menuItem.titleId }

                actionItem.onClick.invoke()
            }
        )
        .build()

    if (headerTitle != null) {
        val headerView = ViewPopupMenuHeaderBinding.inflate(inflater, null, false)
        headerView.popupMenuHeaderTitle.text = headerTitle.resolve(this)
        headerView.popupMenuHeaderCloseButton.setOnClickListener {
            powerMenu.dismiss()
        }
        powerMenu.headerView = headerView.root
    }

    if (footerView != null) {
        powerMenu.footerView = footerView
    }

    if (showAtCenter) {
        powerMenu.showAtCenter(anchorView)
    } else {
        powerMenu.showAsAnchorLeftBottom(anchorView, 0, resources.getDimensionPixelSize(R.dimen.space_small))
    }
}

class PopupMenuItem(
    @StringRes val titleId: Int? = null,
    @StringRes val subTitleId: Int? = null,
    @DrawableRes val startIconId: Int? = null,
    @DrawableRes val endIconId: Int? = null,
    @ColorRes val backgroundColor: Int? = null,
)

data class PopupMenuActionItem(
    val popupMenuItem: PopupMenuItem,
    val onClick: () -> Unit
)
