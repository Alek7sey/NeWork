package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Post
import ru.netology.nework.model.AttachmentModelPost

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val postData: Flow<List<Post>>
    suspend fun readAll()
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, attachmentModel: AttachmentModelPost)
    suspend fun likeById(post: Post)
}