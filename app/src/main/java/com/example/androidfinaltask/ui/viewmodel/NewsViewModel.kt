package com.example.androidfinaltask.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfinaltask.data.api.RetrofitClient
import com.example.androidfinaltask.data.model.Article
import com.example.androidfinaltask.data.model.Author
import com.example.androidfinaltask.data.model.Comment
import com.example.androidfinaltask.data.model.Source
import com.example.androidfinaltask.data.model.Topic
import com.example.androidfinaltask.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _trendingNews = MutableLiveData<List<Article>>()
    val trendingNews: LiveData<List<Article>> = _trendingNews

    private val _latestNews = MutableLiveData<List<Article>>()
    val latestNews: LiveData<List<Article>> = _latestNews

    private val _searchResults = MutableLiveData<List<Article>>()
    val searchResults: LiveData<List<Article>> = _searchResults

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

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

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _articleLikeCount = MutableLiveData<Int>(0)
    val articleLikeCount: LiveData<Int> = _articleLikeCount

    private val _articleCommentCount = MutableLiveData<Int>(0)
    val articleCommentCount: LiveData<Int> = _articleCommentCount

    private val _isArticleLiked = MutableLiveData<Boolean>(false)
    val isArticleLiked: LiveData<Boolean> = _isArticleLiked

    private val _isArticleBookmarked = MutableLiveData<Boolean>(false)
    val isArticleBookmarked: LiveData<Boolean> = _isArticleBookmarked

    init {
        _bookmarkedArticles.value = mutableListOf()
        loadBookmarksFromFirebase()
        loadTopics()
        loadAuthors()
        loadNews()
    }
    
    private fun hasValidImage(article: Article): Boolean {
        val imageUrl = article.imageUrl?.trim()
        return imageUrl != null && 
               imageUrl.isNotEmpty() && 
               (imageUrl.startsWith("http://", ignoreCase = true) || 
                imageUrl.startsWith("https://", ignoreCase = true)) &&
               !imageUrl.equals("null", ignoreCase = true)
    }

    private fun generateArticleId(article: Article): String {
        // Generate a stable ID from URL (most reliable) or title + published date
        // This ensures each article has a unique ID even if API doesn't provide one
        val url = article.url
        if (!url.isNullOrEmpty()) {
            // Use URL as the ID (it's unique per article from NewsAPI)
            return url.hashCode().toString().replace("-", "n")
        }
        // Fallback to title + published date
        val title = article.title
        val publishedAt = article.publishedAt ?: ""
        val combined = "$title$publishedAt"
        return combined.hashCode().toString().replace("-", "n")
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch more articles to ensure we get at least 20 with images
                val response = RetrofitClient.newsApiService.getTopHeadlines(pageSize = 100)
                if (response.isSuccessful) {
                    val allArticles = response.body()?.articles ?: emptyList()
                    // Filter to only include articles with valid images
                    val articlesWithImages = allArticles.filter { hasValidImage(it) }
                        .map { article ->
                            // Generate stable ID from URL or title if ID is null
                            val stableId = article.id ?: generateArticleId(article)
                            // Reset likes and comments to 0 - will be loaded from Firebase
                            article.copy(
                                id = stableId,
                                likes = 0,
                                comments = 0
                            )
                        }
                    
                    // Ensure we have at least 20 posts with images
                    if (articlesWithImages.size >= 20) {
                        _trendingNews.value = articlesWithImages.take(3)
                        _latestNews.value = articlesWithImages.drop(3).take(17) // 3 + 17 = 20 total
                    } else {
                        // If we don't have enough with images, try fetching from different categories
                        loadNewsFromMultipleCategories()
                    }
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
    
    private suspend fun loadNewsFromMultipleCategories() {
        val categories = listOf("general", "business", "technology", "sports", "entertainment", "health", "science")
        val allArticlesWithImages = mutableListOf<Article>()
        
        // Fetch from multiple categories to get enough articles with images
        for (category in categories) {
            if (allArticlesWithImages.size >= 20) break
            
            try {
                val response = RetrofitClient.newsApiService.getTopHeadlines(
                    category = category,
                    pageSize = 50
                )
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    val articlesWithImages = articles.filter { hasValidImage(it) }
                        .map { article ->
                            // Generate stable ID from URL or title if ID is null
                            val stableId = article.id ?: generateArticleId(article)
                            // Reset likes and comments to 0 - will be loaded from Firebase
                            article.copy(
                                id = stableId,
                                likes = 0,
                                comments = 0
                            )
                        }
                    allArticlesWithImages.addAll(articlesWithImages)
                }
            } catch (e: Exception) {
                // Continue with next category
            }
        }
        
        // Remove duplicates based on article title
        val uniqueArticles = allArticlesWithImages.distinctBy { it.title }
        
        if (uniqueArticles.size >= 20) {
            _trendingNews.value = uniqueArticles.take(3)
            _latestNews.value = uniqueArticles.drop(3).take(17) // 3 + 17 = 20 total
        } else {
            // If still not enough, use what we have
            _trendingNews.value = uniqueArticles.take(3)
            _latestNews.value = uniqueArticles.drop(3)
        }
    }

    private fun loadDummyData() {
        // Use placeholder images for dummy data
        val placeholderImageUrl = "https://via.placeholder.com/400x300?text=News+Article"
        val dummyArticles = (1..20).map { index ->
            Article(
                id = "article_$index",
                title = "Sample News Article $index",
                description = "This is a sample news article description for testing purposes.",
                content = "Full content of the article goes here...",
                author = "Author $index",
                source = Source(id = "source_$index", name = "BBC News"),
                imageUrl = placeholderImageUrl,
                url = "https://example.com/article_$index",
                publishedAt = "2024-01-20T10:00:00Z",
                category = "General",
                views = (100..10000).random(),
                likes = 0, // Will be loaded from Firebase
                comments = 0 // Will be loaded from Firebase
            )
        }
        _trendingNews.value = dummyArticles.take(3)
        _latestNews.value = dummyArticles.drop(3).take(17) // 3 + 17 = 20 total
    }

    fun selectArticle(article: Article) {
        // Reset counts when selecting a new article
        _articleLikeCount.value = 0
        _articleCommentCount.value = 0
        _isArticleLiked.value = false
        _isArticleBookmarked.value = false
        
        // Ensure article has an ID
        val articleWithId = if (article.id.isNullOrEmpty()) {
            val generatedId = generateArticleId(article)
            article.copy(id = generatedId)
        } else {
            article
        }
        _selectedArticle.value = articleWithId
        loadArticleData(articleWithId)
    }

    private fun loadArticleData(article: Article) {
        viewModelScope.launch {
            val articleId = article.id
            if (articleId.isNullOrEmpty()) {
                // If still no ID, generate one
                val generatedId = generateArticleId(article)
                loadArticleDataById(generatedId)
            } else {
                loadArticleDataById(articleId)
            }
        }
    }

    private suspend fun loadArticleDataById(articleId: String) {
        // Load like count and status
        val likeCount = FirebaseRepository.getLikeCount(articleId)
        val isLiked = FirebaseRepository.isArticleLiked(articleId)
        _articleLikeCount.value = likeCount
        _isArticleLiked.value = isLiked

        // Load comment count
        val commentCount = FirebaseRepository.getCommentCount(articleId)
        _articleCommentCount.value = commentCount

        // Load bookmark status
        val isBookmarked = FirebaseRepository.isArticleBookmarked(articleId)
        _isArticleBookmarked.value = isBookmarked
    }

    fun loadCommentsForArticle(articleId: String?) {
        viewModelScope.launch {
            if (articleId != null && articleId.isNotEmpty()) {
                try {
                    android.util.Log.d("NewsViewModel", "Loading comments for articleId: $articleId")
                    val commentsList = FirebaseRepository.getCommentsForArticle(articleId)
                    android.util.Log.d("NewsViewModel", "Loaded ${commentsList.size} comments")
                    _comments.value = commentsList
                    _articleCommentCount.value = commentsList.size
                } catch (e: Exception) {
                    android.util.Log.e("NewsViewModel", "Error loading comments: ${e.message}", e)
                    _comments.value = emptyList()
                    _articleCommentCount.value = 0
                }
            } else {
                android.util.Log.e("NewsViewModel", "Cannot load comments: articleId is null or empty")
                _comments.value = emptyList()
                _articleCommentCount.value = 0
            }
        }
    }

    fun addComment(articleId: String?, comment: Comment) {
        viewModelScope.launch {
            if (articleId != null && articleId.isNotEmpty()) {
                try {
                    android.util.Log.d("NewsViewModel", "Adding comment for articleId: $articleId")
                    val result = FirebaseRepository.addComment(articleId, comment)
                    if (result.isSuccess) {
                        android.util.Log.d("NewsViewModel", "Comment added successfully, reloading comments")
                        // Reload comments after adding
                        loadCommentsForArticle(articleId)
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Failed to add comment"
                        android.util.Log.e("NewsViewModel", "Failed to add comment: $errorMsg")
                        _error.value = errorMsg
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NewsViewModel", "Exception adding comment: ${e.message}", e)
                    _error.value = e.message ?: "Error adding comment"
                }
            } else {
                android.util.Log.e("NewsViewModel", "Cannot add comment: articleId is null or empty")
            }
        }
    }

    fun toggleLike(articleId: String?) {
        viewModelScope.launch {
            if (articleId != null && articleId.isNotEmpty()) {
                try {
                    val result = FirebaseRepository.toggleLike(articleId)
                    if (result.isSuccess) {
                        val isLiked = result.getOrNull() ?: false
                        _isArticleLiked.value = isLiked
                        // Immediately update like count
                        val likeCount = FirebaseRepository.getLikeCount(articleId)
                        _articleLikeCount.value = likeCount
                    } else {
                        // Handle error - user might not be logged in
                        val errorMessage = result.exceptionOrNull()?.message ?: "Failed to toggle like"
                        _error.value = errorMessage
                        android.util.Log.e("NewsViewModel", "Toggle like failed: $errorMessage")
                    }
                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Error toggling like"
                    _error.value = errorMessage
                    android.util.Log.e("NewsViewModel", "Toggle like exception: $errorMessage", e)
                }
            } else {
                android.util.Log.e("NewsViewModel", "Cannot toggle like: articleId is null or empty")
            }
        }
    }

    fun toggleBookmark(articleId: String?) {
        viewModelScope.launch {
            if (articleId != null && articleId.isNotEmpty()) {
                try {
                    val result = FirebaseRepository.toggleBookmark(articleId)
                    if (result.isSuccess) {
                        val isBookmarked = result.getOrNull() ?: false
                        _isArticleBookmarked.value = isBookmarked
                        loadBookmarksFromFirebase()
                    } else {
                        // Handle error - user might not be logged in
                        val errorMessage = result.exceptionOrNull()?.message ?: "Failed to toggle bookmark"
                        _error.value = errorMessage
                        android.util.Log.e("NewsViewModel", "Toggle bookmark failed: $errorMessage")
                    }
                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Error toggling bookmark"
                    _error.value = errorMessage
                    android.util.Log.e("NewsViewModel", "Toggle bookmark exception: $errorMessage", e)
                }
            } else {
                android.util.Log.e("NewsViewModel", "Cannot toggle bookmark: articleId is null or empty")
            }
        }
    }

    private fun loadBookmarksFromFirebase() {
        viewModelScope.launch {
            try {
                val bookmarkedIds = FirebaseRepository.getUserBookmarks()
                // Update local bookmarked articles list
                val allArticles = (_trendingNews.value ?: emptyList()) + (_latestNews.value ?: emptyList())
                val bookmarked = allArticles.filter { article -> 
                    article.id != null && bookmarkedIds.contains(article.id)
                }
                _bookmarkedArticles.value = bookmarked.toMutableList()
                
                // Also update bookmark status for currently selected article
                val selectedArticle = _selectedArticle.value
                if (selectedArticle?.id != null) {
                    val isBookmarked = FirebaseRepository.isArticleBookmarked(selectedArticle.id)
                    _isArticleBookmarked.value = isBookmarked
                }
            } catch (e: Exception) {
                // Silently fail - bookmarks might not be available if user not logged in
            }
        }
    }

    fun addBookmark(article: Article) {
        val articleId = article.id
        if (articleId != null) {
            toggleBookmark(articleId)
        } else {
            val current = _bookmarkedArticles.value ?: mutableListOf()
            if (!current.any { it.id == article.id }) {
                current.add(article)
                _bookmarkedArticles.value = current
            }
        }
    }

    fun removeBookmark(article: Article) {
        val articleId = article.id
        if (articleId != null) {
            toggleBookmark(articleId)
        } else {
            val current = _bookmarkedArticles.value ?: mutableListOf()
            current.removeAll { it.id == article.id }
            _bookmarkedArticles.value = current
        }
    }

    fun isBookmarked(article: Article): Boolean {
        val articleId = article.id
        return if (articleId != null && _isArticleBookmarked.value != null) {
            _isArticleBookmarked.value == true
        } else {
            _bookmarkedArticles.value?.any { it.id == article.id } ?: false
        }
    }

    private fun loadTopics() {
        viewModelScope.launch {
            try {
                // Fetch topics from NewsAPI by getting sources with different categories
                val categories = listOf(
                    "business", "entertainment", "general", "health", 
                    "science", "sports", "technology"
                )
                
                val topicsList = mutableListOf<Topic>()
                
                // Fetch sources for each category to get real data
                categories.forEachIndexed { index, category ->
                    try {
                        val response = RetrofitClient.newsApiService.getSources(category = category)
                        if (response.isSuccessful) {
                            val sources = response.body()?.sources ?: emptyList()
                            if (sources.isNotEmpty()) {
                                // Use the first source's description or create a meaningful description
                                val description = sources.firstOrNull()?.description 
                                    ?: "${category.replaceFirstChar { it.uppercase() }} news and updates"
                                
                                topicsList.add(
                                    Topic(
                                        id = (index + 1).toString(),
                                        name = category.replaceFirstChar { it.uppercase() },
                                        description = description,
                                        isSaved = false
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // If API call fails for a category, add a default topic
                        topicsList.add(
                            Topic(
                                id = (index + 1).toString(),
                                name = category.replaceFirstChar { it.uppercase() },
                                description = "${category.replaceFirstChar { it.uppercase() }} news",
                                isSaved = false
                            )
                        )
                    }
                }
                
                // Add additional topics from API if possible
                val additionalCategories = listOf("politics", "lifestyle", "art")
                additionalCategories.forEachIndexed { index, category ->
                    try {
                        val response = RetrofitClient.newsApiService.getSources(category = category)
                        if (response.isSuccessful) {
                            val sources = response.body()?.sources ?: emptyList()
                            if (sources.isNotEmpty()) {
                                val description = sources.firstOrNull()?.description 
                                    ?: "${category.replaceFirstChar { it.uppercase() }} news"
                                
                                topicsList.add(
                                    Topic(
                                        id = (topicsList.size + index + 1).toString(),
                                        name = category.replaceFirstChar { it.uppercase() },
                                        description = description,
                                        isSaved = false
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Skip if API call fails
                    }
                }
                
                // If we got topics from API, use them; otherwise use defaults
                if (topicsList.isNotEmpty()) {
                    _topics.value = topicsList
                } else {
                    loadDefaultTopics()
                }
            } catch (e: Exception) {
                loadDefaultTopics()
            }
        }
    }
    
    private fun loadDefaultTopics() {
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

    private fun loadAuthors() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.newsApiService.getSources()
                if (response.isSuccessful) {
                    val sources = response.body()?.sources ?: emptyList()
                    val authorsList = sources.take(20).mapIndexed { index, source ->
                        // NewsAPI doesn't return logos, so set logo as null
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
        // NewsAPI doesn't return logos, so set logo as null
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

    fun searchArticles(query: String) {
        _searchQuery.value = query.trim()
        val searchTerm = query.trim().lowercase()
        
        if (searchTerm.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // Search in loaded articles first
                val allArticles = (_trendingNews.value ?: emptyList()) + (_latestNews.value ?: emptyList())
                val filteredArticles = allArticles.filter { article ->
                    article.title.lowercase().contains(searchTerm)
                }

                if (filteredArticles.isNotEmpty()) {
                    _searchResults.value = filteredArticles
                } else {
                    // If no results in loaded articles, search via API
                    searchArticlesFromAPI(searchTerm)
                }
            } catch (e: Exception) {
                android.util.Log.e("NewsViewModel", "Error searching articles: ${e.message}", e)
                _searchResults.value = emptyList()
            }
        }
    }

    private suspend fun searchArticlesFromAPI(query: String) {
        try {
            val response = RetrofitClient.newsApiService.getEverything(
                query = query,
                pageSize = 50
            )
            if (response.isSuccessful) {
                val allArticles = response.body()?.articles ?: emptyList()
                val articlesWithImages = allArticles.filter { hasValidImage(it) }
                    .map { article ->
                        val stableId = article.id ?: generateArticleId(article)
                        article.copy(
                            id = stableId,
                            likes = 0,
                            comments = 0
                        )
                    }
                    .filter { article ->
                        article.title.lowercase().contains(query.lowercase())
                    }
                _searchResults.value = articlesWithImages
            } else {
                _searchResults.value = emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("NewsViewModel", "Error searching from API: ${e.message}", e)
            _searchResults.value = emptyList()
        }
    }
}

