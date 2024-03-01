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
import ru.netology.nework.BuildConfig.API_KEY
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
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .addInterceptor { chain ->
            appAuth.authFlow.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .addHeader("Api-Key", API_KEY)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }

            val request = chain.request().newBuilder()
                .addHeader("Api-Key", API_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(okhttp: OkHttpClient): Retrofit =
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