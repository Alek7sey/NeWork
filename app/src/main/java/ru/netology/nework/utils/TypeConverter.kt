package ru.netology.nework.utils

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.entity.EventUserPreviewEmbeddable
import ru.netology.nework.entity.UserPreviewEmbeddable


private val gson = GsonBuilder().create()
private val listType = TypeToken.getParameterized(List::class.java, String::class.java).type
private val mapType = TypeToken.getParameterized(
    Map::class.java,
    Long::class.java,
    UserPreviewEmbeddable::class.java
).type

class TypeConverter {
    @TypeConverter
    fun fromListToString(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun fromStringToList(string: String): List<String> = gson.fromJson(string, listType)

    @TypeConverter
    fun fromMapToString(map: Map<Long, UserPreviewEmbeddable>): String = gson.toJson(map)

    @TypeConverter
    fun fromStringToMap(string: String): Map<Long, UserPreviewEmbeddable> =
        gson.fromJson(string, mapType)

    @TypeConverter
    fun fromMapToStringEvent(map: Map<Long, EventUserPreviewEmbeddable>): String = gson.toJson(map)

    @TypeConverter
    fun fromStringToMapEvent(string: String): Map<Long, EventUserPreviewEmbeddable> =
        gson.fromJson(string, mapType)

    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name

    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)

    @TypeConverter
    fun fromListLongToString(list: List<Long>): String = gson.toJson(list)

    @TypeConverter
    fun toListLongFromString(value: String): List<Long> = gson.fromJson(value, listType)
}