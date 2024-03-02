package ru.netology.nework.repimpl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.EventsApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Media
import ru.netology.nework.entity.AttachmentEventEmbeddable
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.mediator.EventRemoteMediator
import ru.netology.nework.model.AttachmentModelEvent
import ru.netology.nework.repository.EventsRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: EventsApiService,
    keyDao: EventRemoteKeyDao,
    appDb: AppDb,
) : EventsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Event>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.getPagingSource() },
        remoteMediator = EventRemoteMediator(
            apiService = apiService,
            eventDao = eventDao,
            eventRemoteKeyDao = keyDao,
            appDb = appDb
        )
    ).flow.map { it.map(EventEntity::toDto) }

    override val getData: Flow<List<Event>> =
        eventDao.getAllEvents().map(List<EventEntity>::toDto)
            .flowOn(Dispatchers.Default)

    override suspend fun readAll() {
        try {
            val response = apiService.readAllEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val events = response.body() ?: throw ApiError(response.message())
            eventDao.removeAll()
            eventDao.insert(events.map(EventEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val event = eventDao.searchEvent(id)
        try {
            eventDao.removeById(id)
            val response = apiService.deleteEvent(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
        } catch (e: IOException) {
            eventDao.insert(event)
            throw NetworkError
        } catch (e: Exception) {
            eventDao.insert(event)
            throw UnknownError
        }
    }

    override suspend fun save(event: Event) {
        try {
            val response = apiService.saveEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun edit(event: Event) {
        try {
            val response = apiService.editEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun upload(file: File): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )

            val response = apiService.saveMedia(media)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            return response.body() ?: throw ApiError(response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(event: Event, attachmentModel: AttachmentModelEvent) {
        try {
            val media = upload(attachmentModel.file)
            val eventWithAttachment = event.copy(attachment = AttachmentEventEmbeddable(url = media.id, type = attachmentModel.attachmentTypeEvent))
            save(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(event: Event) {
        try {
            eventDao.likedById(event.id)
            val response = when (event.likedByMe) {
                true -> {
                    apiService.unlikeEvent(event.id)
                }

                false -> {
                    apiService.likeEvent(event.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun participatedById(event: Event) {
        try {
            eventDao.participatedById(event.id)
            val response = when (event.participatedByMe) {
                true -> {
                    apiService.refuseParticipants(event.id)
                }

                false -> {
                    apiService.becomeParticipants(event.id)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}