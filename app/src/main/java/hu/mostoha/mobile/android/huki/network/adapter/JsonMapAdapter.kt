package hu.mostoha.mobile.android.huki.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader

class JsonMapAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): Map<String, String>? {
        val result = mutableMapOf<String, String>()

        reader.beginObject()

        while (reader.hasNext()) {
            val key = reader.nextName()
            val value = reader.nextString()
            result[key] = value
        }

        reader.endObject()

        return result.ifEmpty { null }
    }

}
