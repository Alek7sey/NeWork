package ru.netology.nework.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.repimpl.EventRepositoryImpl
import ru.netology.nework.repimpl.JobsRepositoryImpl
import ru.netology.nework.repimpl.MyWallRepositoryImpl
import ru.netology.nework.repimpl.PostRepositoryImpl
import ru.netology.nework.repimpl.UserWallRepositoryImpl
import ru.netology.nework.repimpl.UsersRepositoryImpl
import ru.netology.nework.repository.EventsRepository
import ru.netology.nework.repository.JobsRepository
import ru.netology.nework.repository.MyWallRepository
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.repository.UserWallRepository
import ru.netology.nework.repository.UsersRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPostRepository(postRepositoryImpl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    fun bindUsersRepository(userRepositoryImpl: UsersRepositoryImpl): UsersRepository

    @Binds
    @Singleton
    fun bindEventsRepository(eventRepositoryImpl: EventRepositoryImpl): EventsRepository

    @Binds
    @Singleton
    fun bindJobRepository(jobsRepositoryImpl: JobsRepositoryImpl): JobsRepository

    @Binds
    @Singleton
    fun bindMyWallRepository(myWallRepositoryImpl: MyWallRepositoryImpl): MyWallRepository

    @Binds
    @Singleton
    fun bindUserWallRepository(userWallRepositoryImpl: UserWallRepositoryImpl): UserWallRepository

}