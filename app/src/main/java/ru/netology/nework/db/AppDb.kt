package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dao.MyWallRemoteKeyDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dao.UserWallRemoteKeyDao
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.EventRemoteKeyEntity
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.MyWallRemoteKeyEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.UserWallRemoteKeyEntity
import ru.netology.nework.utils.TypeConverter

@Database(
    entities = [
        EventEntity::class,
        EventRemoteKeyEntity::class,
        JobEntity::class,
        MyWallRemoteKeyEntity::class,
        PostEntity::class,
        PostRemoteKeyEntity::class,
        UserEntity::class,
        UserWallRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConverter::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
    abstract fun myWallRemoteKeyDao(): MyWallRemoteKeyDao
    abstract fun userWallRemoteKeyDao(): UserWallRemoteKeyDao
}