package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String?,
    val content: String,
    val published: String,
    //val likes: Int = 0,
    val likedByMe: Boolean,
    val likeOwnerIds: List<Long>?,
    val mentionedByMe: Boolean,
    val mentionedIds: List<Long>?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbeddable? = null,
    @Embedded
    val coordinates: CoordinatesEmbeddable? = null,
    val users: Map<Long, UserPreviewEmbeddable>
) {
    fun toDto() =
        Post(
            id,
            authorId,
            author,
            authorAvatar,
            authorJob,
            content,
            published,
            //likes,
            likedByMe,
            likeOwnerIds,
            mentionedByMe,
            mentionedIds,
            link,
            ownedByMe,
            attachment,
            coordinates,
            users,
        )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorJob = dto.authorJob,
                content = dto.content,
                published = dto.published,
               // likes = dto.likes,
                likedByMe = dto.likedByMe,
                likeOwnerIds = dto.likeOwnerIds,
                mentionedByMe = dto.mentionedByMe,
                mentionedIds = dto.mentionedIds,
                link = dto.link,
                ownedByMe = dto.ownedByMe,
                attachment = dto.attachment,
                coordinates = dto.coordinates,
                users = dto.users,
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
    val type: AttachmentType
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