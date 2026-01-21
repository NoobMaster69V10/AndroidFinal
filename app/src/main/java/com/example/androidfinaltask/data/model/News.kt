package com.example.androidfinaltask.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("articles")
    val articles: List<Article>,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("totalResults")
    val totalResults: Int? = null
)

data class SourcesResponse(
    @SerializedName("sources")
    val sources: List<Source>,
    @SerializedName("status")
    val status: String? = null
)

data class Article(
    val id: String?,
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    @SerializedName("source")
    val source: Source?,
    @SerializedName("urlToImage")
    val imageUrl: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("publishedAt")
    val publishedAt: String?,
    val category: String? = null,
    val views: Int? = 0,
    val likes: Int? = 0,
    val comments: Int? = 0
)

data class Source(
    val id: String?,
    val name: String,
    val description: String? = null,
    val url: String? = null,
    val category: String? = null,
    val language: String? = null,
    val country: String? = null
)

data class Topic(
    val id: String,
    val name: String,
    val description: String?,
    val isSaved: Boolean = false
)

data class Author(
    val id: String,
    val name: String,
    val logo: String?,
    val followers: String?,
    val isFollowing: Boolean = false
)

data class Comment(
    val id: String,
    val authorName: String,
    val authorImage: String?,
    val content: String,
    val replies: List<Comment>?,
    val timestamp: String?,
    val articleId: String? = null,
    val userId: String? = null
)

