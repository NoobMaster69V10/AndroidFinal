package com.example.androidfinaltask.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val website: String = "",
    val profileImageUrl: String = "",
    val country: String = "",
    val selectedTopics: List<String> = emptyList(),
    val followedSources: List<String> = emptyList(),
    val bookmarkedArticles: List<String> = emptyList()
)


