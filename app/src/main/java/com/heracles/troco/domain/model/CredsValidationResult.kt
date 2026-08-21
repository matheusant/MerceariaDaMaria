package com.heracles.troco.domain.model

import com.heracles.troco.ui.screen.SignupErrors

/** Resultado da validação de credenciais de login (puro, testável). */
sealed class CredsValidationResult {
    data object Valid : CredsValidationResult()
    data class Invalid(val error: SignupErrors) : CredsValidationResult()
}
