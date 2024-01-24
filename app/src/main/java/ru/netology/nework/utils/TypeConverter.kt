package ru.netology.nework.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.entity.EventUserPreviewEmbeddable
import ru.netology.nework.entity.UserPreviewEmbeddable


private val listType = TypeToken.getParameterized(List::class.java, String::class.java).type
private val mapType = TypeToken.getParameterized(
    Map::class.java,
    Long::class.java,
    UserPreviewEmbeddable::class.java
).type

class TypeConverter {
    @TypeConverter
    fun fromListToString(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun fromStringToList(string: String): List<String> = Gson().fromJson(string, listType)

    @TypeConverter
    fun fromMapToString(map: Map<Long, UserPreviewEmbeddable>): String = Gson().toJson(map)

    @TypeConverter
    fun fromStringToMap(string: String): Map<Long, UserPreviewEmbeddable> {
        val type = object : TypeToken<Map<Long, UserPreviewEmbeddable>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromMapToStringEvent(map: Map<Long, EventUserPreviewEmbeddable>): String = Gson().toJson(map)

    @TypeConverter
    fun fromStringToMapEvent(string: String): Map<Long, EventUserPreviewEmbeddable> {
        val type = object : TypeToken<Map<Long, EventUserPreviewEmbeddable>>() {}.type
        return Gson().fromJson(string, type)
    }

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromListIntToString(list: List<Int>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListIntFromString(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, type)
    }
}