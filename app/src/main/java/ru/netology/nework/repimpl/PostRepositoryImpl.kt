package ru.netology.nework.repimpl

import android.util.Log
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
import ru.netology.nework.api.PostApiService
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.AttachmentEmbeddable
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.error.*
import ru.netology.nework.mediator.PostRemoteMediator
import ru.netology.nework.model.AttachmentModelPost
import ru.netology.nework.repository.PostRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: PostApiService,
    keyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = keyDao,
            appDb = appDb
        )
    ).flow.map { it.map(PostEntity::toDto) }

    override val postData: Flow<List<Post>> =
        postDao.getAllPosts().map(List<PostEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun readAll() {
        try {
            val response = apiService.readAll()
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val posts = response.body() ?: throw ApiError(response.message())
            postDao.removeAll()
            postDao.insert(posts.map(PostEntity::fromDto))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val post = postDao.searchPost(id)
        try {
            postDao.removeById(id)
            val response = apiService.deletePost(id)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
        } catch (e: IOException) {
            postDao.insert(post)
            throw NetworkError
        } catch (e: Exception) {
            postDao.insert(post)
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = apiService.savePost(post)
            if (!response.isSuccessful) {
                throw ApiError(response.message())
            }
            val body = response.body() ?: throw ApiError(response.message())
            postDao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, attachmentModel: AttachmentModelPost) {
        try {
            val media = upload(attachmentModel.file)
            Log.d("Logging", "post media:${media.id}")
            val postWithAttachment = post.copy(attachment = AttachmentEmbeddable(url = media.id, type = attachmentModel.attachmentTypePost))
            Log.d("Logging", "post PostRepositoryIMpl:${postWithAttachment.content}")
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(post: Post) {
        try {
            postDao.likedById(post.id)
            val response = when (post.likedByMe) {
                true -> {
                    apiService.unlikePost(post.id)
                }

                false -> {
                    apiService.likePost(post.id)
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

    private suspend fun upload(file: File): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )
//            Log.d("Logging", "upload до отправки на сервер медиа:${media.headers}")
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
}