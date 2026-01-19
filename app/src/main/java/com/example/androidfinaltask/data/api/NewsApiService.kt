package com.example.androidfinaltask.data.api

import com.example.androidfinaltask.data.model.NewsResponse
import com.example.androidfinaltask.data.model.SourcesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("everything")
    suspend fun getEverything(
        @Query("q") query: String = "news",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String = "4aea63ec7df94324be458da577e4ef63"
    ): Response<NewsResponse>

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String = "4aea63ec7df94324be458da577e4ef63"
    ): Response<NewsResponse>

    @GET("sources")
    suspend fun getSources(
        @Query("category") category: String? = null,
        @Query("language") language: String = "en",
        @Query("country") country: String? = null,
        @Query("apiKey") apiKey: String = "4aea63ec7df94324be458da577e4ef63"
    ): Response<SourcesResponse>
}

