package com.firstapp.dogscanai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import network.model.AuthManager
import network.model.AuthRepository
import network.model.AuthResponse
import network.model.ProfileResponse

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    private val _profileResult = MutableLiveData<Result<ProfileResponse>>()
    val profileResult: LiveData<Result<ProfileResponse>> = _profileResult

    private val _logoutResult = MutableLiveData<Result<Unit>>()
    val logoutResult: LiveData<Result<Unit>> = _logoutResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.register(name, email, password)
            _registerResult.value = result

            // Save user if successful
            result.onSuccess { authResponse ->
                authResponse.token?.let { token ->
                    authResponse.user?.let { user ->
                        AuthManager.saveUser(token, user)
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(email, password)
            _loginResult.value = result

            // Save user if successful
            result.onSuccess { authResponse ->
                authResponse.token?.let { token ->
                    authResponse.user?.let { user ->
                        AuthManager.saveUser(token, user)
                    }
                }
            }
            _isLoading.value = false
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getProfile()
            _profileResult.value = result
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.logout()
            _logoutResult.value = result
            _isLoading.value = false
        }
    }
}