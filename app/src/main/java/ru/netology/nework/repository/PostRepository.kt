package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val postData: Flow<List<Post>>
    suspend fun readAll()
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)

    //    suspend fun edit(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun likeById(post: Post)
//    suspend fun shareById(id: Long)
}