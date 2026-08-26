package com.heracles.troco.ui.viewmodel

import com.heracles.troco.MainDispatcherRule
import com.heracles.troco.domain.model.AuthResult
import com.heracles.troco.domain.model.User
import com.heracles.troco.domain.repository.AuthRepository
import com.heracles.troco.domain.repository.SignupRepository
import com.heracles.troco.domain.usecase.auth.SaveUserUseCase
import com.heracles.troco.domain.usecase.auth.ValidateCredentialsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeAuthRepository(
        override val usuarioAtual: String? = null,
        var signupResult: AuthResult = AuthResult.Success("uid-123")
    ) : AuthRepository {
        var signupCallCount = 0
        var receivedPhone: String? = null
        var receivedPassword: String? = null

        override suspend fun signupWithPhone(
            phone: String,
            password: String
        ): AuthResult {
            signupCallCount++
            receivedPhone = phone
            receivedPassword = password
            return signupResult
        }

        override suspend fun loginWithPhone(
            phone: String,
            password: String
        ): AuthResult = AuthResult.Error("Operação não utilizada neste teste")

        override fun sair() = Unit
    }

    private class FakeSignupRepository(
        private val saveResult: AuthResult = AuthResult.Success("uid-123")
    ) : SignupRepository {
        val savedUsers = mutableListOf<User>()

        override fun observe(): Flow<User?> = flowOf(null)

        override suspend fun saveUserInfo(user: User): AuthResult {
            savedUsers += user
            return saveResult
        }
    }

    @Test
    fun `estado inicial nao autenticado quando repositorio nao possui usuario`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.autenticado)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `estado inicial autenticado quando repositorio possui usuario`() {
        val viewModel = createViewModel(
            authRepository = FakeAuthRepository(usuarioAtual = "uid-existente")
        )

        assertTrue(viewModel.uiState.value.autenticado)
    }

    @Test
    fun `alteracoes dos campos atualizam o estado`() {
        val viewModel = createViewModel()

        viewModel.onNameChange("Maria")
        viewModel.onLastnameChange("Silva")
        viewModel.onPhoneChange("(11) 98765-4321")
        viewModel.onPasswordChange("senha123")

        val state = viewModel.uiState.value
        assertEquals("Maria", state.name)
        assertEquals("Silva", state.lastName)
        assertEquals("11987654321", state.phone)
        assertEquals("senha123", state.password)
    }

    @Test
    fun `telefone com mais de onze digitos nao substitui valor valido`() {
        val viewModel = createViewModel()
        viewModel.onPhoneChange("11987654321")

        viewModel.onPhoneChange("119876543210")

        assertEquals("11987654321", viewModel.uiState.value.phone)
    }

    @Test
    fun `cadastro invalido exibe erros e nao chama repositorios`() = runTest {
        val authRepository = FakeAuthRepository()
        val signupRepository = FakeSignupRepository()
        val viewModel = createViewModel(authRepository, signupRepository)

        viewModel.signUp()

        val state = viewModel.uiState.value
        assertEquals("Informe um nome válido", state.error.name)
        assertEquals("Informe um sobrenome válido", state.error.lastName)
        assertEquals("Informe um telefone válido", state.error.phone)
        assertEquals("A senha deve conter no minímo 8 caracteres", state.error.password)
        assertFalse(state.isLoading)
        assertFalse(state.autenticado)
        assertEquals(0, authRepository.signupCallCount)
        assertTrue(signupRepository.savedUsers.isEmpty())
    }

    @Test
    fun `cadastro valido autentica e salva dados do usuario`() = runTest {
        val authRepository = FakeAuthRepository(
            signupResult = AuthResult.Success("uid-novo")
        )
        val signupRepository = FakeSignupRepository()
        val viewModel = createViewModel(authRepository, signupRepository)
        fillValidForm(viewModel)

        viewModel.signUp()

        val state = viewModel.uiState.value
        assertEquals(1, authRepository.signupCallCount)
        assertEquals("11987654321", authRepository.receivedPhone)
        assertEquals("senha123", authRepository.receivedPassword)
        assertEquals(
            listOf(User(name = "Maria", lastName = "Silva", phone = "11987654321")),
            signupRepository.savedUsers
        )
        assertTrue(state.autenticado)
        assertFalse(state.isLoading)
        assertEquals("", state.error.name)
        assertEquals("", state.error.lastName)
        assertEquals("", state.error.phone)
        assertEquals("", state.error.password)
    }

    @Test
    fun `erro no cadastro encerra carregamento exibe mensagem e nao salva usuario`() = runTest {
        val authRepository = FakeAuthRepository(
            signupResult = AuthResult.Error("Telefone já cadastrado")
        )
        val signupRepository = FakeSignupRepository()
        val viewModel = createViewModel(authRepository, signupRepository)
        fillValidForm(viewModel)

        viewModel.signUp()

        val state = viewModel.uiState.value
        assertEquals(1, authRepository.signupCallCount)
        assertTrue(signupRepository.savedUsers.isEmpty())
        assertFalse(state.autenticado)
        assertFalse(state.isLoading)
        assertEquals("Telefone já cadastrado", state.error.password)
    }

    private fun createViewModel(
        authRepository: FakeAuthRepository = FakeAuthRepository(),
        signupRepository: FakeSignupRepository = FakeSignupRepository()
    ) = SignupViewModel(
        authCredential = ValidateCredentialsUseCase(),
        saveUserUseCase = SaveUserUseCase(signupRepository),
        authRepository = authRepository
    )

    private fun fillValidForm(viewModel: SignupViewModel) {
        viewModel.onNameChange("Maria")
        viewModel.onLastnameChange("Silva")
        viewModel.onPhoneChange("(11) 98765-4321")
        viewModel.onPasswordChange("senha123")
    }
}
