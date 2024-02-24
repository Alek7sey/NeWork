package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName
import ru.netology.nework.entity.AttachmentEmbeddable
import ru.netology.nework.entity.CoordinatesEmbeddable
import ru.netology.nework.entity.UserPreviewEmbeddable
import java.io.Serializable

data class Post(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    @SerializedName("coords")
    val coordinates: CoordinatesEmbeddable? = null,
    val link: String? = null,
    val mentionIds: List<Int>?,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val attachment: AttachmentEmbeddable? = null,
    val users: Map<Long, UserPreviewEmbeddable>,
    val ownedByMe: Boolean = false,
) : Serializable

data class Coordinates(
    val latitude: String,
    val longitude: String
)

data class Attachment(
    val url: String,
    val type: AttachmentTypePost,
)

data class UserPreview(
    val name: String,
    val avatar: String?
)