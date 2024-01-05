package ru.netology.nework.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.MyWallApiService
import ru.netology.nework.dao.MyWallRemoteKeyDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.entity.MyWallRemoteKeyEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class MyWallRemoteMediator(
    private val apiService: MyWallApiService,
    private val dao: PostDao,
    private val remoteKey: MyWallRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    if (dao.isEmpty() && remoteKey.isEmpty()) {
                        apiService.getLatestMyWall(state.config.initialLoadSize)
                    } else {
                        val id = remoteKey.max() ?: return MediatorResult.Success(false)
                        apiService.getAfterMyWall(id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = remoteKey.max() ?: return MediatorResult.Success(true)
                    apiService.getAfterMyWall(id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = remoteKey.min() ?: return MediatorResult.Success(false)
                    apiService.getBeforeMyWall(id, state.config.pageSize)
                }
            }

            if (!result.isSuccessful) {
                throw ApiError(result.message())
            }
            val body = result.body() ?: throw ApiError(result.message())

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        if (remoteKey.isEmpty()) {
                            //   postRemoteKeyDao.clear()
                            remoteKey.insert(
                                listOf(
                                    MyWallRemoteKeyEntity(
                                        MyWallRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id,
                                    ),
                                    MyWallRemoteKeyEntity(
                                        MyWallRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id,
                                    )
                                )
                            )
                        } else {
                            remoteKey.insert(
                                MyWallRemoteKeyEntity(
                                    MyWallRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        remoteKey.insert(
                            MyWallRemoteKeyEntity(
                                MyWallRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        remoteKey.insert(
                            MyWallRemoteKeyEntity(
                                MyWallRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                }
                dao.insert(body.map(PostEntity::fromDto))

            }

            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}