package hu.mostoha.mobile.android.huki.model.ui

import androidx.annotation.StringRes

data class InputField(
    val inputValue: String = "",
    @StringRes val errorResId: Int? = null,
)
