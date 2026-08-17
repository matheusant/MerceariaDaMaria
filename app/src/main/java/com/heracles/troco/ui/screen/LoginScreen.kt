package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.R
import com.heracles.troco.components.ButtonComponent
import com.heracles.troco.components.ClickableTextComponent
import com.heracles.troco.components.HeaderTextComponent
import com.heracles.troco.components.SubtitleTextComponent
import com.heracles.troco.components.TrocoTextField
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.TerracottaSecondary

@Composable
fun LoginScreen(
    onSignupSelected: () -> Unit,
    onSignIn: () -> Unit
) {
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderTextComponent(text = stringResource(R.string.welcome_back))
            Spacer(modifier = Modifier.height(12.dp))
            SubtitleTextComponent(text = stringResource(R.string.hello_subtitle))

            Spacer(modifier = Modifier.height(20.dp))

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
                placeholder = "Digite sua senha",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Esqueci minha senha",
                textAlign = TextAlign.End,
                style = TextStyle(
                    fontFamily = DMSansFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TerracottaSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ButtonComponent(
                value = stringResource(R.string.login),
                onClick = onSignIn
            )

            Spacer(modifier = Modifier.height(20.dp))

            ClickableTextComponent(
                initialText = "Não tem conta? ",
                clickableText = "Cadastre-se",
                onTextSelected = onSignupSelected
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onSignupSelected = {},
        onSignIn = {}
    )
}