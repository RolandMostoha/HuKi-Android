package hu.mostoha.mobile.android.huki.extensions

import java.io.File
import java.io.InputStream

fun getOrCreateDirectory(parent: String, child: String): File? {
    val file = File(parent, child)
    return if (!file.exists()) {
        val isSuccess = file.mkdirs()
        if (isSuccess) file else null
    } else {
        file
    }
}

fun File.copyFrom(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}