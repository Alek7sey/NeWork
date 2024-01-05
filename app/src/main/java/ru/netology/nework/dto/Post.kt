package ru.netology.nework.dto

import ru.netology.nework.entity.AttachmentEmbeddable
import ru.netology.nework.entity.CoordinatesEmbeddable
import ru.netology.nework.entity.UserPreviewEmbeddable

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String?,
    val content: String,
    val published: String,
   // val likes: Int = 0,
    val likedByMe: Boolean,
    val likeOwnerIds: List<Long>?,
    val mentionedByMe: Boolean,
    val mentionedIds: List<Long>?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val attachment: AttachmentEmbeddable? = null,
    val coordinates: CoordinatesEmbeddable? = null,
    val users: Map<Long, UserPreviewEmbeddable>,
)

data class Coordinates(
    val latitude: String,
    val longitude: String
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

data class UserPreview(
    val name: String,
    val avatar: String?
)