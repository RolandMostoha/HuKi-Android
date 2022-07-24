package hu.mostoha.mobile.android.huki.extensions

import android.content.res.Resources
import androidx.annotation.RawRes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.InputStream

fun getOrCreateDirectory(parent: String, child: String): File? {
    val file = File(parent, child)

    return if (!file.exists()) {
        val isSuccess = file.mkdirs()

        if (isSuccess) {
            file
        } else {
            null
        }
    } else {
        file
    }
}

fun File.copyFrom(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

inline fun <reified T> Resources.readRawJson(@RawRes rawResId: Int): T {
    this.openRawResource(rawResId).bufferedReader().use { bufferedReader ->
        return Gson().fromJson(bufferedReader, object : TypeToken<T>() {}.type)
    }
}
