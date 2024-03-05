package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentEvent
import ru.netology.nework.dto.AttachmentTypeEvent
import ru.netology.nework.dto.CoordinatesEvent
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventUserPreview

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    @Embedded
    val coordinates: CoordinatesEventEmbeddable? = null,
    val type: String,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val speakerIds: List<Int>?,
    val participantsIds: List<Int>?,
    val participatedByMe: Boolean,
    @Embedded(prefix = "foo_")
    val attachment: AttachmentEventEmbeddable? = null,
    val link: String? = null,
    val users: Map<Long, EventUserPreviewEmbeddable>,
    val ownedByMe: Boolean = false
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorJob,
        authorAvatar,
        content,
        datetime,
        published,
        coordinates,
        type,
        likeOwnerIds,
        likedByMe,
        speakerIds,
        participantsIds,
        participatedByMe,
        attachment,
        link,
        users,
        ownedByMe
    )

    companion object {
        fun fromDto(event: Event) =
            EventEntity(
                event.id,
                event.authorId,
                event.author,
                event.authorJob,
                event.authorAvatar,
                event.content,
                event.datetime,
                event.published,
                event.coordinates,
                event.type,
                event.likeOwnerIds,
                event.likedByMe,
                event.speakerIds,
                event.participantsIds,
                event.participatedByMe,
                event.attachment,
                event.link,
                event.users,
                event.ownedByMe
            )
    }
}

data class CoordinatesEventEmbeddable(
    val latitude: String,
    val longitude: String
) {
    fun toDto() = CoordinatesEvent(latitude, longitude)

    companion object {
        fun fromDto(dto: CoordinatesEvent) = CoordinatesEventEmbeddable(dto.latitude, dto.longitude)
    }
}

data class AttachmentEventEmbeddable(
    val url: String,
    val type: AttachmentTypeEvent
) {
    fun toDto() = AttachmentEvent(url, type)

    companion object {
        fun fromDto(dto: AttachmentEvent) = AttachmentEventEmbeddable(dto.url, dto.type)
    }
}

data class EventUserPreviewEmbeddable(
    val name: String,
    val avatar: String?
) {
    fun toDto() = EventUserPreview(name, avatar)

    companion object {
        fun fromDto(dto: EventUserPreview) = EventUserPreviewEmbeddable(dto.name, dto.avatar)
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)