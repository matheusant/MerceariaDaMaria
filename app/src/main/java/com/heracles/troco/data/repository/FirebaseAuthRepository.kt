package com.heracles.troco.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.heracles.troco.data.remote.awaitResult
import com.heracles.troco.di.IoDispatcher
import com.heracles.troco.domain.model.AuthResult
import com.heracles.troco.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @IoDispatcher private val io: CoroutineDispatcher
): AuthRepository {
    override val usuarioAtual: String?
        get() = auth.currentUser?.uid

    override suspend fun signupWithPhone(
        phone: String,
        password: String
    ): AuthResult {
        val email = "$phone@troco.com"
        return executar { auth.createUserWithEmailAndPassword(email, password) }
    }

    override suspend fun loginWithPhone(
        phone: String,
        password: String
    ): AuthResult {
        val email = "$phone@troco.com"
        return executar { auth.signInWithEmailAndPassword(email, password) }
    }

    override fun sair() {
        auth.signOut()
    }

    private suspend fun executar(
        acao: () -> Task<com.google.firebase.auth.AuthResult>
    ): AuthResult = withContext(io) {
        try {
            val resultado = acao().awaitResult()
            val uid = resultado.user?.uid
            if (uid != null) AuthResult.Success(uid)
            else AuthResult.Error("Não foi possível identificar o usuário.")
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Credenciais inválidas.")
        } catch (_: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Usuário já cadastrado.")
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Falha na autenticação.")
        }
    }
}