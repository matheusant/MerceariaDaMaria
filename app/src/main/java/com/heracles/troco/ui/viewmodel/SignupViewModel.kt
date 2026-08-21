package com.heracles.troco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heracles.troco.domain.entity.SignupEntity
import com.heracles.troco.domain.model.AuthResult
import com.heracles.troco.domain.model.CredsValidationResult
import com.heracles.troco.domain.repository.AuthRepository
import com.heracles.troco.domain.usecase.auth.ValidateCredentialsUseCase
import com.heracles.troco.ui.screen.AuthUiState
import com.heracles.troco.ui.screen.SignupErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authCredential: ValidateCredentialsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(AuthUiState(autenticado = authRepository.usuarioAtual != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        if (digitsOnly.length <= 11) {
            _uiState.update { it.copy(phone = digitsOnly, error = SignupErrors(phone = "")) }
        }
    }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, error = SignupErrors(password = "")) }

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, error = SignupErrors(name = "")) }

    fun onLastnameChange(value: String) =
        _uiState.update { it.copy(lastName = value, error = SignupErrors(lastName = "")) }

    fun signUp() = authenticatePhone { phone, password ->
        authRepository.signupWithPhone(
            phone = phone,
            password = password
        )
    }

    private fun authenticatePhone(action: suspend (phone: String, password: String) -> AuthResult) {
        val state = _uiState.value
        val phone = state.phone.trim()
        val user = SignupEntity(
            name = state.name,
            lastName = state.lastName,
            phone = phone,
            password = state.password
        )
        when (val validate = authCredential(user)) {
            is CredsValidationResult.Invalid -> {
                _uiState.update { it.copy(error = validate.error) }
            }

            is CredsValidationResult.Valid -> {
                _uiState.update { it.copy(isLoading = true, error = SignupErrors()) }
                viewModelScope.launch {
                    applyResult(action(phone, state.password))
                }
            }
        }
    }

    private fun applyResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> _uiState.update {
                it.copy(
                    isLoading = false,
                    autenticado = true
                )
            }

            is AuthResult.Error -> _uiState.update {
                it.copy(isLoading = false, error = SignupErrors().apply { password = result.message })
            }
        }
    }
}