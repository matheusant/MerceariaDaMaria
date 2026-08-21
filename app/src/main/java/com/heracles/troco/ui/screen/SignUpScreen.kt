package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.R
import com.heracles.troco.components.ButtonComponent
import com.heracles.troco.components.ClickableTextComponent
import com.heracles.troco.components.HeaderTextComponent
import com.heracles.troco.components.SubtitleTextComponent
import com.heracles.troco.components.TrocoTextField
import com.heracles.troco.ui.theme.RubikFontFamily

@Composable
fun SignUpScreen(
    state: AuthUiState,
    onNameChange: (String) -> Unit,
    onLastnameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignup: () -> Unit,
    onLoginSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderTextComponent(text = stringResource(R.string.create_account))
            Spacer(modifier = Modifier.height(12.dp))
            SubtitleTextComponent(text = stringResource(R.string.signup_subtitle))

            Spacer(modifier = Modifier.height(20.dp))

            TrocoTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Nome",
                placeholder = "Seu nome",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text,
                hasError = state.error.name,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = state.lastName,
                onValueChange = onLastnameChange,
                label = "Sobrenome",
                placeholder = "Seu sobrenome",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text,
                hasError = state.error.lastName,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                isPhone = true,
                label = stringResource(R.string.phone),
                placeholder = stringResource(R.string.phone_placeholder),
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                hasError = state.error.phone,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                isPassword = true,
                label = stringResource(R.string.password),
                placeholder = "Crie uma senha forte",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                hasError = state.error.password,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonComponent(
                content = {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = stringResource(R.string.create_account),
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontFamily = RubikFontFamily
                            ),
                        )
                    }
                },
                onClick = onSignup
            )

            Spacer(modifier = Modifier.height(20.dp))

            ClickableTextComponent(
                initialText = "Já tem uma conta? ",
                clickableText = "Entrar",
                onTextSelected = onLoginSelected
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        state = AuthUiState(),
        onLoginSelected = {},
        onPhoneChange = {},
        onNameChange = {},
        onLastnameChange = {},
        onPasswordChange = {},
        onSignup = {}
    )
}