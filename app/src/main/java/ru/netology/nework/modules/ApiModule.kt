package ru.netology.nework.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nework.BuildConfig
import ru.netology.nework.api.EventsApiService
import ru.netology.nework.api.JobApiService
import ru.netology.nework.api.MyWallApiService
import ru.netology.nework.api.PostApiService
import ru.netology.nework.api.UserApiService
import ru.netology.nework.api.UserWallApiService
import ru.netology.nework.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun provideOkHttp(
        appAuth: AppAuth,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .addInterceptor { chain ->
            val request = appAuth.authFlow.value?.token?.let { token ->
                chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
            } ?: chain.request()

            chain.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(okhttp: OkHttpClient) =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okhttp)
            .build()

    @Singleton
    @Provides
    fun providePostApiService(retrofit: Retrofit): PostApiService = retrofit.create<PostApiService>()

    @Singleton
    @Provides
    fun provideUserApiService(retrofit: Retrofit): UserApiService = retrofit.create<UserApiService>()

    @Singleton
    @Provides
    fun provideEventsApiService(retrofit: Retrofit): EventsApiService = retrofit.create<EventsApiService>()

    @Singleton
    @Provides
    fun provideJobsApiService(retrofit: Retrofit): JobApiService = retrofit.create<JobApiService>()

    @Singleton
    @Provides
    fun provideMyWallApiService(retrofit: Retrofit): MyWallApiService = retrofit.create<MyWallApiService>()

    @Singleton
    @Provides
    fun provideUserWallApiService(retrofit: Retrofit): UserWallApiService = retrofit.create<UserWallApiService>()

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/api/"
    }

}