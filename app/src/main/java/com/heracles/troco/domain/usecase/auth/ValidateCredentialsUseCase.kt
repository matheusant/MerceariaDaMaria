package com.heracles.troco.domain.usecase.auth

import com.heracles.troco.domain.entity.SignupEntity
import com.heracles.troco.domain.model.CredsValidationResult
import com.heracles.troco.ui.screen.SignupErrors
import javax.inject.Inject

class ValidateCredentialsUseCase @Inject constructor() {

    operator fun invoke(user: SignupEntity): CredsValidationResult {
        val error = SignupErrors()
        if (!PHONE_REGEX.matches(user.phone.trim())) {
            error.phone = "Informe um telefone válido"
        }
        if (user.password.trim().length < MIN_SENHA) {
            error.password =
                "A senha deve conter no minímo 8 caracteres"
        }
        if (user.name.trim().isEmpty() || user.name.length < 3) {
            error.name = "Informe um nome válido"
        }
        if (user.lastName.trim().isEmpty() || user.lastName.length < 3) {
            error.lastName = "Informe um sobrenome válido"
        }


        return if (error.name.isEmpty() && error.lastName.isEmpty() && error.phone.isEmpty() && error.password.isEmpty()) {
            CredsValidationResult.Valid
        } else {
            CredsValidationResult.Invalid(error)
        }
    }

    companion object {
//        private val PASS_REGEX =
//            Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$") // Pelo menos 6 caracteres, pelo menos uma letra e um número
        const val MIN_SENHA = 8
        private val PHONE_REGEX = Regex("^([1-9][0-9])(9[1-9][0-9]{7}|[2-5][0-9]{7})\$")
    }
}