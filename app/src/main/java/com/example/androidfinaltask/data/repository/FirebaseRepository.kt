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
}

