package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import hu.mostoha.mobile.android.huki.extensions.collapse
import hu.mostoha.mobile.android.huki.extensions.hide

open class BottomSheetDialog(private val binding: ViewBinding) {

    protected val context: Context = binding.root.context
    protected val resources: Resources = context.resources

    private val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(binding.root)

    open fun updateInset(insets: Insets) {
        binding.root.updatePadding(bottom = insets.bottom)
    }

    fun hide() {
        bottomSheetBehavior.hide()
    }

    fun show() {
        bottomSheetBehavior.collapse()
    }

    fun isExpanded(): Boolean {
        return bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED
    }

    fun addStateListener(onExpanded: (Int) -> Unit) {
        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    onExpanded.invoke(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            }
        )
    }

}
