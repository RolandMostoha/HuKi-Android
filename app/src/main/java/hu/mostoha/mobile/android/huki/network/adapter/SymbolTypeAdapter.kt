package hu.mostoha.mobile.android.huki.network.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import hu.mostoha.mobile.android.huki.model.network.SymbolType

object SymbolTypeAdapter {

    @FromJson
    fun fromJson(jsonReader: JsonReader, delegate: JsonAdapter<SymbolType>): SymbolType {
        return try {
            delegate.fromJson(jsonReader) ?: SymbolType.UNHANDLED
        } catch (exception: JsonDataException) {
            SymbolType.UNHANDLED
        }
    }

}