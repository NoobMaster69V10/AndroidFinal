package com.example.androidfinaltask.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfinaltask.data.model.User
import com.example.androidfinaltask.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _authState = MutableLiveData<AuthState>(AuthState.NotAuthenticated)
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthState()
    }

    fun checkAuthState() {
        if (FirebaseRepository.isUserLoggedIn()) {
            val userId = FirebaseRepository.getCurrentUserId()
            userId?.let {
                _authState.value = AuthState.Success
                loadUserData(it)
            }
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }

    fun signUp(email: String, password: String, username: String) {
        Log.d("AuthViewModel", "signUp called with email: $email")
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                Log.d("AuthViewModel", "Calling FirebaseRepository.signUp")
                val result = FirebaseRepository.signUp(email, password, username)
                result.onSuccess {
                    Log.d("AuthViewModel", "Sign up successful, userId: $it")
                    loadUserData(it)
                    _authState.value = AuthState.Success
                }.onFailure { exception ->
                    Log.e("AuthViewModel", "Sign up failed: ${exception.message}", exception)
                    _authState.value = AuthState.Error(exception.message ?: "Sign up failed")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Exception in signUp: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = FirebaseRepository.signIn(email, password)
            result.onSuccess {
                _authState.value = AuthState.Success
                loadUserData(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Sign in failed")
            }
        }
    }

    fun signOut() {
        FirebaseRepository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.NotAuthenticated
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                FirebaseRepository.updateUserInFirestore(user)
                _currentUser.value = user
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = FirebaseRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Failed to send reset email")
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            val user = FirebaseRepository.getUserFromFirestore(uid)
            _currentUser.value = user
        }
    }
}

sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

