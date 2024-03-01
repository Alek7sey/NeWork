package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun searchPost(id: Long): PostEntity

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id=:id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun savePost(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likedById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}