package hu.mostoha.mobile.android.huki.extensions

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.RawRes
import androidx.core.net.toFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
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

fun Context.readText(filePath: String): String? {
    try {
        val inputStream = assets.open(filePath)
        val size = inputStream.available()
        val buffer = ByteArray(size)

        inputStream.read(buffer)
        inputStream.close()

        return String(buffer, Charsets.UTF_8)
    } catch (ioException: IOException) {
        Timber.e(ioException, "Failed to read text from file: $filePath")

        return null
    }
}

inline fun <reified T> Resources.readRawJson(@RawRes rawResId: Int): T {
    this.openRawResource(rawResId).bufferedReader().use { bufferedReader ->
        return Gson().fromJson(bufferedReader, object : TypeToken<T>() {}.type)
    }
}

/**
 * Gets the file name for different types of [Uri]s:
 *  1. Content provider uri [content://com.example.app/sample.png]
 *  2. File uri [file://data/user/0/com.example.app/cache/sample.png]
 *  3. Resource uri [android.resource://com.example.app/1234567890] or [android.resource://com.example.app/raw/sample]
 *  4. Http uri [https://example.com/sample.png]
 *
 *  Source: https://stackoverflow.com/a/71546366/11153276
 */
@Suppress("ThrowsCount")
fun Uri.getFileName(context: Context): String {
    return when (scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            // Like: Content provider uri [content://com.example.app/sample.png]
            val cursor = context.contentResolver.query(
                this,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            ) ?: throw FileNotFoundException("Failed to obtain cursor from the content resolver")

            cursor.moveToFirst()
            if (cursor.count == 0) {
                throw FileNotFoundException("The given Uri doesn't represent any file")
            }
            val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val displayName = cursor.getString(displayNameColumnIndex)
            cursor.close()

            displayName.removeFileExtension()
        }
        ContentResolver.SCHEME_FILE -> {
            // Like [file://data/user/0/com.example.app/cache/sample.png]
            toFile().nameWithoutExtension
        }
        ContentResolver.SCHEME_ANDROID_RESOURCE -> {
            // Like [android.resource://com.example.app/1234567890] or [android.resource://com.example.app/raw/sample]
            var resourceId = lastPathSegment?.toIntOrNull()
            if (resourceId != null) {
                return context.resources.getResourceName(resourceId)
            }
            // Like [android.resource://com.example.app/raw/sample]
            val packageName = authority
            val resourceType = if (pathSegments.size >= 1) {
                pathSegments[0]
            } else {
                throw FileNotFoundException("Resource type could not be found")
            }
            val resourceEntryName = if (pathSegments.size >= 2) {
                pathSegments[1]
            } else {
                throw FileNotFoundException("Resource entry name could not be found")
            }
            resourceId = context.resources.getIdentifier(
                resourceEntryName,
                resourceType,
                packageName
            )

            context.resources.getResourceName(resourceId)
        }
        else -> {
            // Http uri [https://example.com/sample.png]
            toString().removeFileExtension().substringAfterLast("/")
        }
    }
}

fun String.removeFileExtension(): String {
    return this.substringBeforeLast(".")
}
