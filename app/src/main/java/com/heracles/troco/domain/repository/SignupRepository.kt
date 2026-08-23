package com.heracles.troco.domain.repository

import com.heracles.troco.domain.model.AuthResult
import com.heracles.troco.domain.model.User
import kotlinx.coroutines.flow.Flow

interface SignupRepository {
    fun observe(): Flow<User?>
    suspend fun saveUserInfo(user: User): AuthResult
}