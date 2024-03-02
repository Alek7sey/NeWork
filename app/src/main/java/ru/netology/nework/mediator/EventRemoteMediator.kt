package ru.netology.nework.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.EventsApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.error.ApiError

@ExperimentalPagingApi
class EventRemoteMediator(
    private val apiService: EventsApiService,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, EventEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    if (eventDao.isEmpty() && eventRemoteKeyDao.isEmpty()) {
                        apiService.getLatestEvents(state.config.initialLoadSize)
                    } else {
                        val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                        apiService.getAfterEvents(id, state.config.pageSize)
                    }

                }

                LoadType.PREPEND -> {
                    val id = eventRemoteKeyDao.max() ?: return MediatorResult.Success(true)
                    apiService.getAfterEvents(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = eventRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBeforeEvents(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.message())
            }
            val body = result.body() ?: throw ApiError(result.message())

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (eventRemoteKeyDao.isEmpty()) {
                            eventRemoteKeyDao.insert(
                                listOf(
                                    EventRemoteKeyEntity(
                                        EventRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id,
                                    ),
                                    EventRemoteKeyEntity(
                                       EventRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id,
                                    )
                                )
                            )
                        } else {
                            eventRemoteKeyDao.insert(
                                EventRemoteKeyEntity(
                                    EventRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        eventRemoteKeyDao.insert(
                            EventRemoteKeyEntity(
                                EventRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                }
                eventDao.insert(body.map(EventEntity::fromDto))

            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}