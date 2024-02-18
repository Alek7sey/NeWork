package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Post

interface UserWallRepository {
    val data: Flow<PagingData<Post>>
    suspend fun readAllFromUserWall(authorId: Long): List<Post>
}