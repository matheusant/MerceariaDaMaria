package com.heracles.troco.ui.screen

data class SignupErrors(
    var name: String = "",
    var lastName: String = "",
    var phone: String = "",
    var password: String = ""
)

data class AuthUiState (
    val name: String = "",
    val lastName: String = "",
    val phone: String = "",
    val password: String = "",
    val error: SignupErrors = SignupErrors(),
    val isLoading: Boolean = false,
    val autenticado: Boolean = false
)