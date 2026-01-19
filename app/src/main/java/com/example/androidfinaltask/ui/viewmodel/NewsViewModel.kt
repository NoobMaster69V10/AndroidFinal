package com.example.androidfinaltask.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfinaltask.data.api.RetrofitClient
import com.example.androidfinaltask.data.model.Article
import com.example.androidfinaltask.data.model.Author
import com.example.androidfinaltask.data.model.Source
import com.example.androidfinaltask.data.model.Topic
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _trendingNews = MutableLiveData<List<Article>>()
    val trendingNews: LiveData<List<Article>> = _trendingNews

    private val _latestNews = MutableLiveData<List<Article>>()
    val latestNews: LiveData<List<Article>> = _latestNews

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedArticle = MutableLiveData<Article?>()
    val selectedArticle: LiveData<Article?> = _selectedArticle

    private val _bookmarkedArticles = MutableLiveData<MutableList<Article>>()
    val bookmarkedArticles: LiveData<MutableList<Article>> = _bookmarkedArticles

    private val _topics = MutableLiveData<List<Topic>>()
    val topics: LiveData<List<Topic>> = _topics

    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> = _authors

    init {
        _bookmarkedArticles.value = mutableListOf()
        loadTopics()
        loadAuthors()
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.newsApiService.getTopHeadlines(pageSize = 10)
                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.take(10) ?: emptyList()
                    _trendingNews.value = articles.take(3)
                    _latestNews.value = articles.drop(3)
                } else {
                    _error.value = "Failed to load news"
                    loadDummyData()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                loadDummyData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadDummyData() {
        val dummyArticles = (1..10).map { index ->
            Article(
                id = "article_$index",
                title = "Sample News Article $index",
                description = "This is a sample news article description for testing purposes.",
                content = "Full content of the article goes here...",
                author = "Author $index",
                source = Source(id = "source_$index", name = "BBC News"),
                imageUrl = null,
                publishedAt = "2024-01-20T10:00:00Z",
                category = "General",
                views = (100..10000).random(),
                likes = (10..1000).random(),
                comments = (5..500).random()
            )
        }
        _trendingNews.value = dummyArticles.take(3)
        _latestNews.value = dummyArticles.drop(3)
    }

    fun selectArticle(article: Article) {
        _selectedArticle.value = article
    }

    fun addBookmark(article: Article) {
        val current = _bookmarkedArticles.value ?: mutableListOf()
        if (!current.any { it.id == article.id }) {
            current.add(article)
            _bookmarkedArticles.value = current
        }
    }

    fun removeBookmark(article: Article) {
        val current = _bookmarkedArticles.value ?: mutableListOf()
        current.removeAll { it.id == article.id }
        _bookmarkedArticles.value = current
    }

    fun isBookmarked(article: Article): Boolean {
        return _bookmarkedArticles.value?.any { it.id == article.id } ?: false
    }

    private fun loadTopics() {
        viewModelScope.launch {
            try {
                val categories = listOf(
                    "business", "entertainment", "general", "health", 
                    "science", "sports", "technology"
                )
                
                val topicsList = categories.mapIndexed { index, category ->
                    Topic(
                        id = (index + 1).toString(),
                        name = category.replaceFirstChar { it.uppercase() },
                        description = "${category.replaceFirstChar { it.uppercase() }} news",
                        isSaved = false
                    )
                }
                
                val additionalTopics = listOf(
                    Topic("8", "National", "National news", false),
                    Topic("9", "International", "International news", false),
                    Topic("10", "Politics", "Politics news", false),
                    Topic("11", "Lifestyle", "Lifestyle news", false),
                    Topic("12", "Art", "Art news", false)
                )
                
                _topics.value = topicsList + additionalTopics
            } catch (e: Exception) {
                val defaultTopics = listOf(
                    Topic("1", "Business", "Business news", false),
                    Topic("2", "Entertainment", "Entertainment news", false),
                    Topic("3", "General", "General news", false),
                    Topic("4", "Health", "Health news", false),
                    Topic("5", "Science", "Science news", false),
                    Topic("6", "Sports", "Sports news", false),
                    Topic("7", "Technology", "Technology news", false)
                )
                _topics.value = defaultTopics
            }
        }
    }

    private fun loadAuthors() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.newsApiService.getSources()
                if (response.isSuccessful) {
                    val sources = response.body()?.sources ?: emptyList()
                    val authorsList = sources.take(20).mapIndexed { index, source ->
                        Author(
                            id = source.id ?: index.toString(),
                            name = source.name,
                            logo = null,
                            followers = "${(100..5000).random()}k",
                            isFollowing = false
                        )
                    }
                    _authors.value = authorsList
                } else {
                    loadDefaultAuthors()
                }
            } catch (e: Exception) {
                loadDefaultAuthors()
            }
        }
    }

    private fun loadDefaultAuthors() {
        val defaultAuthors = listOf(
            Author("1", "BBC News", null, "1.5M", false),
            Author("2", "CNN", null, "900k", false),
            Author("3", "Reuters", null, "1.2M", false),
            Author("4", "The New York Times", null, "2.1M", false),
            Author("5", "The Guardian", null, "1.8M", false),
            Author("6", "Associated Press", null, "1.1M", false),
            Author("7", "The Washington Post", null, "1.3M", false)
        )
        _authors.value = defaultAuthors
    }

    fun toggleTopicSave(topic: Topic) {
        val current = _topics.value ?: emptyList()
        val updated = current.map {
            if (it.id == topic.id) it.copy(isSaved = !it.isSaved) else it
        }
        _topics.value = updated
    }

    fun toggleAuthorFollow(author: Author) {
        val current = _authors.value ?: emptyList()
        val updated = current.map {
            if (it.id == author.id) it.copy(isFollowing = !it.isFollowing) else it
        }
        _authors.value = updated
    }
}

