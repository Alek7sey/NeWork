package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentEvent
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.CoordinatesEvent
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventUserPreview

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String,
    val content: String,
    val type: String,
    val dateEvent: String,
    val published: String,
   // val likes: Int = 0,
    val likedByMe: Boolean,
    val likeOwnerIds: List<Long>?,
    val participatedByMe: Boolean,
    val participantsIds: List<Long>?,
   // val participateCnt: Int = 0,
    val speakersIds: List<Long>?,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    @Embedded(prefix = "foo_")
    val attachment: AttachmentEventEmbeddable? = null,
    @Embedded
    val coordinates: CoordinatesEventEmbeddable? = null,
    val users: Map<Long, EventUserPreviewEmbeddable>,
) {
    fun toDto() = Event(
        id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        type,
        dateEvent,
        published,
      //  likes,
        likedByMe,
        likeOwnerIds,
        participatedByMe,
        participantsIds,
      //  participateCnt,
        speakersIds,
        link,
        ownedByMe,
        attachment,
        coordinates,
        users,
    )

    companion object {
        fun fromDto(event: Event) =
            EventEntity(
                event.id,
                event.authorId,
                event.author,
                event.authorAvatar,
                event.authorJob,
                event.content,
                event.type,
                event.dateEvent,
                event.published,
            //    event.likes,
                event.likedByMe,
                event.likeOwnerIds,
                event.participatedByMe,
                event.participantsIds,
             //   event.participateCnt,
                event.speakersIds,
                event.link,
                event.ownedByMe,
                event.attachment,
                event.coordinates,
                event.users,
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
    val type: AttachmentType
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