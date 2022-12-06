package com.abc.mirroring.cast.shared.utils

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import javax.inject.Inject

class GsonParser @Inject constructor(private val gson: Gson) {
    fun <T>toJson(data: T):String{
        return gson.toJson(data)
    }

    fun <T> fromJson(data: String,  classOfT: Class<T> ): T{
        return gson.fromJson(data, classOfT)
    }
}

object UriTypeAdapter : JsonDeserializer<Uri>, JsonSerializer<Uri> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
        return Uri.parse(json?.asString.toString())
    }

    override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}