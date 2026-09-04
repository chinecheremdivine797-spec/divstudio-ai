package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(private val userDao: UserDao) {

    private val _currentUserId = MutableStateFlow<String?>("user_default_01")
    val currentUserId = _currentUserId.asStateFlow()

    fun getCurrentUserFlow(): Flow<UserEntity?> {
        val id = _currentUserId.value ?: "user_default_01"
        return userDao.getUserById(id)
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun login(email: String, password: String):Result<UserEntity> {
        val user = userDao.getUserByEmail(email)
        return if (user != null) {
            _currentUserId.value = user.id
            Result.success(user)
        } else {
            // If user doesn't exist, create account directly to provide smooth experience
            val newUser = UserEntity(
                id = "user_${UUID.randomUUID().toString().take(8)}",
                email = email,
                fullName = email.substringBefore("@").replace(".", " ").capitalize(),
                role = if (email.contains("admin", ignoreCase = true) || email == "divstudio03@gmail.com") "admin" else "user",
                creditsRemaining = 100,
                planName = "Studio Creator Pro"
            )
            userDao.insertUser(newUser)
            _currentUserId.value = newUser.id
            Result.success(newUser)
        }
    }

    suspend fun register(email: String, fullName: String, password: String): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val newUser = UserEntity(
            id = "user_${UUID.randomUUID().toString().take(8)}",
            email = email,
            fullName = fullName.ifBlank { "Creator" },
            role = if (email.contains("admin", ignoreCase = true) || email == "divstudio03@gmail.com") "admin" else "user",
            creditsRemaining = 100,
            planName = "Studio Creator Pro"
        )
        userDao.insertUser(newUser)
        _currentUserId.value = newUser.id
        return Result.success(newUser)
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
    }

    fun logout() {
        _currentUserId.value = null
    }

    suspend fun updateProfile(name: String, defaultStyle: String, defaultRatio: String, defaultVoice: String) {
        val id = _currentUserId.value ?: return
        val current = userDao.getUserByEmail(id) ?: return
        val updated = current.copy(
            fullName = name,
            defaultStyle = defaultStyle,
            defaultRatio = defaultRatio,
            defaultVoice = defaultVoice
        )
        userDao.updateUser(updated)
    }
}
