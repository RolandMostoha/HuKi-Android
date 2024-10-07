package hu.mostoha.mobile.android.huki.model.ui

sealed class ProductEvents {

    data class Error(val error: Message.Res) : ProductEvents()

}
