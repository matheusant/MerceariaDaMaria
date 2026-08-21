package com.heracles.troco.domain.repository

import com.heracles.troco.domain.model.AuthResult

interface AuthRepository {
    val usuarioAtual: String?

    suspend fun signupWithPhone(phone: String, password: String) : AuthResult
    suspend fun loginWithPhone(phone: String, password: String) : AuthResult

    fun sair()
}