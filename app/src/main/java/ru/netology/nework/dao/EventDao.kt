package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAllEvents(): Flow<List<EventEntity>>
    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun searchEvent(id: Long): EventEntity

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getPagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("UPDATE EventEntity SET content = :content WHERE id=:id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun saveEvent(event: EventEntity) =
        if (event.id == 0L) insert(event) else updateContentById(event.id, event.content)

    @Query(
        """
        UPDATE EventEntity SET
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likedById(id: Long)

    @Query(
        """
        UPDATE EventEntity SET
        participatedByMe = CASE WHEN participatedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun participatedById(id: Long)


    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM EventEntity")
    suspend fun removeAll()


}