package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Event
import java.io.File

interface EventsRepository {
    val data: Flow<PagingData<Event>>
    val getData: Flow<List<Event>>
    suspend fun readAll()
    suspend fun removeById(id: Long)
    suspend fun save(event: Event)
    suspend fun edit(event: Event)
    suspend fun saveWithAttachment(event: Event, file: File)
    suspend fun likeById(event: Event)
    suspend fun participatedById(event: Event)
}