package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heracles.troco.R
import com.heracles.troco.components.ButtonComponent
import com.heracles.troco.components.ClickableTextComponent
import com.heracles.troco.components.HeaderTextComponent
import com.heracles.troco.components.SubtitleTextComponent
import com.heracles.troco.components.TrocoTextField

@Composable
fun SignUpScreen(
    onLoginSelected: () -> Unit
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
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
                value = name,
                onValueChange = { name = it },
                label = "Nome",
                placeholder = "Seu nome",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Sobrenome",
                placeholder = "Seu sobrenome",
                leadingIcon = Icons.Outlined.Person,
                keyboardType = KeyboardType.Text,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = phone,
                onValueChange = { phone = it },
                label = stringResource(R.string.phone),
                placeholder = stringResource(R.string.phone_placeholder),
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = password,
                onValueChange = { password = it },
                isPassword = true,
                label = stringResource(R.string.password),
                placeholder = "Crie uma senha forte",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonComponent(
                value = stringResource(R.string.register),
                onClick = {}
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
    SignUpScreen(onLoginSelected = {})
}