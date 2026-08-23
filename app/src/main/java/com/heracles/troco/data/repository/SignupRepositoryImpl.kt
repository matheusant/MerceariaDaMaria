package com.heracles.troco.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.heracles.troco.data.remote.FirestoreUserMapper
import com.heracles.troco.di.IoDispatcher
import com.heracles.troco.domain.model.AuthResult
import com.heracles.troco.domain.model.User
import com.heracles.troco.domain.repository.SignupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firestoreMapper: FirestoreUserMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SignupRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<User?> =
        authUidFlow()
            .flatMapLatest { uid -> if (uid != null) fetchUserFromFirestore(uid) else flowOf(null) }

    override suspend fun saveUserInfo(user: User): AuthResult = withContext(ioDispatcher) {
        try {
            val uid = auth.currentUser?.uid
                ?: return@withContext AuthResult.Error("Usuário não autenticado")

            val userMap = firestoreMapper.fromUser(user)

            firestore.collection(USER_COLLECTION)
                .document(uid)
                .set(userMap)
                .await()

            AuthResult.Success(uid)
        } catch (e: Exception) {
            AuthResult.Error(e.toString())
        }
    }

    private fun authUidFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun fetchUserFromFirestore(uid: String): Flow<User?> = callbackFlow {
        val subscription = firestore.collection(USER_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                    val user = firestoreMapper.toUser(snapshot.data!!)
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    companion object {
        private const val USER_COLLECTION = "users"
    }
}