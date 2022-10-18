package hu.mostoha.mobile.android.huki.views

import android.content.Context
import android.view.View
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import hu.mostoha.mobile.android.huki.extensions.collapse
import hu.mostoha.mobile.android.huki.extensions.hide

open class BottomSheetDialog(binding: ViewBinding) {

    private val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(binding.root)

    protected val context: Context = binding.root.context

    fun hide() {
        bottomSheetBehavior.hide()
    }

    fun show() {
        bottomSheetBehavior.collapse()
    }

}
