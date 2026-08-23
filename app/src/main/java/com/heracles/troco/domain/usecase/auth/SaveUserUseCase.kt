package com.heracles.troco.domain.usecase.auth

import com.heracles.troco.domain.model.User
import com.heracles.troco.domain.repository.SignupRepository
import javax.inject.Inject

class SaveUserUseCase @Inject constructor(
    private val signupRepository: SignupRepository
) {
    suspend operator fun invoke(user: User) = signupRepository.saveUserInfo(user)
}