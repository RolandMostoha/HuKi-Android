package hu.mostoha.mobile.android.turistautak.extensions

import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService

fun EditText.clearFocusAndHideKeyboard() {
    clearFocus()
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}