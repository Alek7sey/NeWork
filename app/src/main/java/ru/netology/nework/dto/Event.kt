package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName
import ru.netology.nework.entity.AttachmentEventEmbeddable
import ru.netology.nework.entity.CoordinatesEventEmbeddable
import ru.netology.nework.entity.EventUserPreviewEmbeddable
import java.io.Serializable

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    @SerializedName("coords")
    val coordinates: CoordinatesEventEmbeddable? = null,
    val type: String,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val speakerIds: List<Int>?,
    val participantsIds: List<Int>?,
    val participatedByMe: Boolean,
    val attachment: AttachmentEventEmbeddable? = null,
    val link: String? = null,
    val users: Map<Long, EventUserPreviewEmbeddable>,
) : Serializable

data class CoordinatesEvent(
    val latitude: String,
    val longitude: String
)

data class AttachmentEvent(
    val url: String,
    val type: AttachmentTypeEvent
)

data class EventUserPreview(
    val name: String,
    val avatar: String?
)