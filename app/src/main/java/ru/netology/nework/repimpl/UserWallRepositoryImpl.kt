package ru.netology.nework.repimpl

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.UserWallApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.UserWallRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.mediator.UserWallRemoteMediator
import ru.netology.nework.repository.UserWallRepository
import java.io.IOException

class UserWallRepositoryImpl(
    private val dao: PostDao,
    private val apiService: UserWallApiService,
    keyDao: UserWallRemoteKeyDao,
    appDb: AppDb,
    private val post: Post
) : UserWallRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = UserWallRemoteMediator(
            apiService = apiService,
            dao = dao,
            remoteKey = keyDao,
            appDb = appDb,
            post = post
        )
    ).flow.map {
        it.map(PostEntity::toDto)
    }

    override suspend fun readAll() {
        try {
            val response = apiService.readUserWall(post.authorId)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val posts = response.body() ?: throw ApiError(response.message())
            dao.removeAll()
            dao.insert(posts.map(PostEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}