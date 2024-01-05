package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.MyWallRemoteKeyEntity

@Dao
interface MyWallRemoteKeyDao {

    @Query("SELECT MAX(`key`) FROM MyWallRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT MIN(`key`) FROM MyWallRemoteKeyEntity")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myWallRemoteKeyEntity: MyWallRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myWallRemoteKeyEntity: List<MyWallRemoteKeyEntity>)

    @Query("DELETE FROM MyWallRemoteKeyEntity")
    suspend fun clear()

    @Query("SELECT COUNT(*) == 0 FROM MyWallRemoteKeyEntity")
    suspend fun isEmpty(): Boolean
}