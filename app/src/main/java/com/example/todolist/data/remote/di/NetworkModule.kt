package com.example.todolist.data.remote.di

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.todolist.data.remote.service.AiApiService

object NetworkModule {
    fun provideRetrofit(baseUrl: String, apiKey: String? = null): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)

        if (!apiKey.isNullOrBlank()) {
            val authInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(req)
            }
            clientBuilder.addInterceptor(authInterceptor)
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideAiApiService(retrofit: Retrofit): AiApiService {
        return retrofit.create(AiApiService::class.java)
    }
}
