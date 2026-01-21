package com.example.androidfinaltask.data.repository

import android.util.Log
import com.example.androidfinaltask.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseRepository {
    private val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting FirebaseAuth instance: ${e.message}")
            throw e
        }
    }
    
    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting Firestore instance: ${e.message}")
            throw e
        }
    }
    
    private const val USERS_COLLECTION = "users"
    private const val COMMENTS_COLLECTION = "comments"
    private const val LIKES_COLLECTION = "likes"
    private const val BOOKMARKS_COLLECTION = "bookmarks"
    
    fun checkFirebaseInitialization(): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Firebase not initialized: ${e.message}")
            false
        }
    }

    suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            if (!checkFirebaseInitialization()) {
                return Result.failure(Exception("Firebase is not initialized. Please check your google-services.json file."))
            }
            
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("User creation failed"))
            val user = User(
                uid = userId,
                username = username,
                email = email
            )
            saveUserToFirestore(user)
            Result.success(userId)
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email is already registered"
                "ERROR_OPERATION_NOT_ALLOWED" -> "Sign up is disabled. Please enable Email/Password authentication in Firebase Console"
                else -> e.message ?: "Firebase authentication error: ${e.errorCode}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            val errorMessage = e.message ?: e.localizedMessage ?: "Unknown error occurred"
            Log.e("FirebaseRepository", "Sign up error: $errorMessage", e)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return Result.failure(Exception("Sign in failed"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserFromFirestore(uid: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserInFirestore(user: User) {
        try {
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(articleId: String, comment: com.example.androidfinaltask.data.model.Comment): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("FirebaseRepository", "User not logged in - cannot add comment")
                return Result.failure(Exception("User not logged in. Please log in to comment."))
            }
            
            Log.d("FirebaseRepository", "Adding comment: articleId=$articleId, userId=$userId, content=${comment.content}")
            
            val commentData = hashMapOf(
                "id" to comment.id,
                "articleId" to articleId,
                "userId" to userId,
                "authorName" to comment.authorName,
                "authorImage" to (comment.authorImage ?: ""),
                "content" to comment.content,
                "timestamp" to (comment.timestamp ?: System.currentTimeMillis().toString())
            )
            firestore.collection(COMMENTS_COLLECTION)
                .document(comment.id)
                .set(commentData)
                .await()
            
            Log.d("FirebaseRepository", "Comment added successfully: ${comment.id}")
            Result.success(comment.id)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding comment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getCommentsForArticle(articleId: String): List<com.example.androidfinaltask.data.model.Comment> {
        return try {
            Log.d("FirebaseRepository", "Loading comments for articleId: $articleId")
            val snapshot = firestore.collection(COMMENTS_COLLECTION)
                .whereEqualTo("articleId", articleId)
                .get()
                .await()
            
            Log.d("FirebaseRepository", "Found ${snapshot.size()} comments for articleId: $articleId")
            
            val comments = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                try {
                    com.example.androidfinaltask.data.model.Comment(
                        id = data?.get("id") as? String ?: doc.id,
                        authorName = data?.get("authorName") as? String ?: "",
                        authorImage = data?.get("authorImage") as? String,
                        content = data?.get("content") as? String ?: "",
                        replies = null,
                        timestamp = data?.get("timestamp") as? String,
                        articleId = data?.get("articleId") as? String,
                        userId = data?.get("userId") as? String
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseRepository", "Error parsing comment ${doc.id}: ${e.message}")
                    null
                }
            }
            
            comments.sortedByDescending { it.timestamp?.toLongOrNull() ?: 0L }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error loading comments: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getCommentCount(articleId: String): Int {
        return try {
            val snapshot = firestore.collection(COMMENTS_COLLECTION)
                .whereEqualTo("articleId", articleId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun toggleLike(articleId: String): Result<Boolean> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("FirebaseRepository", "User not logged in - cannot toggle like")
                return Result.failure(Exception("User not logged in. Please log in to like articles."))
            }
            
            Log.d("FirebaseRepository", "Toggling like: articleId=$articleId, userId=$userId")
            
            val likeDocId = "${articleId}_${userId}"
            val likeRef = firestore.collection(LIKES_COLLECTION).document(likeDocId)
            val snapshot = likeRef.get().await()
            
            if (snapshot.exists()) {
                likeRef.delete().await()
                Log.d("FirebaseRepository", "Like removed successfully")
                Result.success(false)
            } else {
                likeRef.set(hashMapOf(
                    "articleId" to articleId,
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                Log.d("FirebaseRepository", "Like added successfully")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error toggling like: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun isArticleLiked(articleId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val likeDocId = "${articleId}_${userId}"
            val snapshot = firestore.collection(LIKES_COLLECTION)
                .document(likeDocId)
                .get()
                .await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLikeCount(articleId: String): Int {
        return try {
            val snapshot = firestore.collection(LIKES_COLLECTION)
                .whereEqualTo("articleId", articleId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun toggleBookmark(articleId: String): Result<Boolean> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e("FirebaseRepository", "User not logged in - cannot toggle bookmark")
                return Result.failure(Exception("User not logged in. Please log in to bookmark articles."))
            }
            
            Log.d("FirebaseRepository", "Toggling bookmark: articleId=$articleId, userId=$userId")
            
            val bookmarkDocId = "${articleId}_${userId}"
            val bookmarkRef = firestore.collection(BOOKMARKS_COLLECTION).document(bookmarkDocId)
            val snapshot = bookmarkRef.get().await()
            
            if (snapshot.exists()) {
                bookmarkRef.delete().await()
                updateUserBookmarks(userId, articleId, false)
                Log.d("FirebaseRepository", "Bookmark removed successfully")
                Result.success(false)
            } else {
                bookmarkRef.set(hashMapOf(
                    "articleId" to articleId,
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                updateUserBookmarks(userId, articleId, true)
                Log.d("FirebaseRepository", "Bookmark added successfully")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error toggling bookmark: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun isArticleBookmarked(articleId: String): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val bookmarkDocId = "${articleId}_${userId}"
            val snapshot = firestore.collection(BOOKMARKS_COLLECTION)
                .document(bookmarkDocId)
                .get()
                .await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserBookmarks(): List<String> {
        return try {
            val userId = getCurrentUserId() ?: return emptyList()
            val snapshot = firestore.collection(BOOKMARKS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.getString("articleId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun updateUserBookmarks(userId: String, articleId: String, add: Boolean) {
        try {
            val user = getUserFromFirestore(userId)
            if (user != null) {
                val bookmarks = user.bookmarkedArticles.toMutableList()
                if (add && !bookmarks.contains(articleId)) {
                    bookmarks.add(articleId)
                } else if (!add) {
                    bookmarks.remove(articleId)
                }
                val updatedUser = user.copy(bookmarkedArticles = bookmarks)
                updateUserInFirestore(updatedUser)
            }
        } catch (e: Exception) {
        }
    }
}

