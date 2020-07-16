package hu.mostoha.mobile.android.turistautak.extensions

import timber.log.Timber
import java.io.File

fun getOrCreateDirectory(parent: String, child: String): File? {
    val file = File(parent, child)
    return if (!file.exists()) {
        val isSuccess = file.mkdirs()
        Timber.d("Created directory ${file.path} : $isSuccess")
        if (isSuccess) {
            file
        } else {
            null
        }
    } else {
        file
    }
}