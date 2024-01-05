package ru.netology.nework.dto

import ru.netology.nework.entity.AttachmentEventEmbeddable
import ru.netology.nework.entity.CoordinatesEventEmbeddable
import ru.netology.nework.entity.EventUserPreviewEmbeddable

data class Event(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String,
    val content: String,
    val type: String,
    val dateEvent: String,
    val published: String,
    //val likes: Int = 0,
    val likedByMe: Boolean,
    val likeOwnerIds: List<Long>?,
    val participatedByMe: Boolean,
    val participantsIds: List<Long>?,
   // val participateCnt: Int = 0,
    val speakersIds: List<Long>?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val attachment: AttachmentEventEmbeddable? = null,
    val coordinates: CoordinatesEventEmbeddable? = null,
    val users: Map<Long, EventUserPreviewEmbeddable>,
)

data class CoordinatesEvent(
    val latitude: String,
    val longitude: String
)

data class AttachmentEvent(
    val url: String,
    val type: AttachmentType
)

data class EventUserPreview(
    val name: String,
    val avatar: String?
)