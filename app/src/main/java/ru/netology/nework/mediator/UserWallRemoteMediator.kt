package ru.netology.nework.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.UserWallApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserWallRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.UserWallRemoteKeyEntity
import ru.netology.nework.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class UserWallRemoteMediator(
    private val apiService: UserWallApiService,
    private val dao: PostDao,
    private val remoteKey: UserWallRemoteKeyDao,
    private val appDb: AppDb,
) : RemoteMediator<Int, PostEntity>() {

    val post: Post? = null
    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    if (dao.isEmpty() && remoteKey.isEmpty()) {
                        apiService.getLatestUserWall(post?.authorId!!, state.config.initialLoadSize)
                    } else {
                        val id = remoteKey.max() ?: return MediatorResult.Success(false)
                        apiService.getAfterUserWall(post?.authorId!!, id, state.config.pageSize)
                    }
                }

                LoadType.PREPEND -> {
                    val id = remoteKey.max() ?: return MediatorResult.Success(true)
                    apiService.getAfterUserWall(post?.authorId!!, id, state.config.pageSize)
                }

                LoadType.APPEND -> {
                    val id = remoteKey.min() ?: return MediatorResult.Success(false)
                    apiService.getBeforeUserWall(post?.authorId!!, id, state.config.pageSize)
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
                            remoteKey.insert(
                                listOf(
                                    UserWallRemoteKeyEntity(
                                        UserWallRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id,
                                    ),
                                    UserWallRemoteKeyEntity(
                                        UserWallRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id,
                                    )
                                )
                            )
                        } else {
                            remoteKey.insert(
                                UserWallRemoteKeyEntity(
                                    UserWallRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                )
                            )
                        }
                    }

                    LoadType.PREPEND -> {
                        remoteKey.insert(
                            UserWallRemoteKeyEntity(
                                UserWallRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        remoteKey.insert(
                            UserWallRemoteKeyEntity(
                                UserWallRemoteKeyEntity.KeyType.BEFORE,
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