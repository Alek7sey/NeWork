package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.UserWallRemoteKeyEntity

@Dao
interface UserWallRemoteKeyDao {

    @Query("SELECT MAX(`key`) FROM UserWallRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT MIN(`key`) FROM UserWallRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userWallRemoteKeyEntity: UserWallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userWallRemoteKeyEntity: List<UserWallRemoteKeyEntity>)

    @Query("DELETE FROM UserWallRemoteKeyEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) == 0 FROM UserWallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean
}