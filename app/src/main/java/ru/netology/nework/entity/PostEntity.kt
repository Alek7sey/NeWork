package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentTypePost
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String?,
    val authorAvatar: String? = null,
    val content: String,
    val published: String,
    @Embedded
    val coordinates: CoordinatesEmbeddable? = null,
    val link: String? = null,
    val mentionIds: List<Int>?,
    val mentionedMe: Boolean,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    val ownedByMe: Boolean = false,
    val users: Map<Long, UserPreviewEmbeddable>,
    val likes: Int
) {
    fun toDto() =
        Post(
            id,
            authorId,
            author,
            authorJob,
            authorAvatar,
            content,
            published,
            coordinates,
            link,
            mentionIds,
            mentionedMe,
            likeOwnerIds,
            likedByMe,
            attachment,
            users,
            ownedByMe,
            likes
        )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorJob = dto.authorJob,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                coordinates = dto.coordinates,
                link = dto.link,
                mentionIds = dto.mentionIds,
                mentionedMe = dto.mentionedMe,
                likeOwnerIds = dto.likeOwnerIds,
                likedByMe = dto.likedByMe,
                attachment = dto.attachment,
                ownedByMe = dto.ownedByMe,
                users = dto.users,
                likes = dto.likes
            )
    }
}

data class CoordinatesEmbeddable(
    val latitude: String,
    val longitude: String
) {
    fun toDto() = Coordinates(latitude, longitude)

    companion object {
        fun fromDto(dto: Coordinates) = CoordinatesEmbeddable(dto.latitude, dto.longitude)
    }
}

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentTypePost
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment) = AttachmentEmbeddable(dto.url, dto.type)
    }
}

data class UserPreviewEmbeddable(
    val name: String,
    val avatar: String?
) {
    fun toDto() = UserPreview(name, avatar)

    companion object {
        fun fromDto(dto: UserPreview) = UserPreviewEmbeddable(dto.name, dto.avatar)
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)