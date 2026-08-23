package com.heracles.troco.domain.usecase.auth

import com.heracles.troco.domain.model.User
import com.heracles.troco.domain.repository.SignupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserUseCase @Inject constructor(
    private val signupRepository: SignupRepository
) {
    operator fun invoke(): Flow<User?> = signupRepository.observe()
}