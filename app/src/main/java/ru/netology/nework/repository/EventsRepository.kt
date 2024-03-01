package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event
import ru.netology.nework.model.AttachmentModelEvent

interface EventsRepository {
    val data: Flow<PagingData<Event>>
    val getData: Flow<List<Event>>
    suspend fun readAll()
    suspend fun removeById(id: Long)
    suspend fun save(event: Event)
    suspend fun edit(event: Event)
    suspend fun saveWithAttachment(event: Event, attachmentModel: AttachmentModelEvent)
    suspend fun likeById(event: Event)
    suspend fun participatedById(event: Event)
}