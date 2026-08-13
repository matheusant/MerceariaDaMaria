package com.example.merceariadamaria.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merceariadamaria.R
import com.example.merceariadamaria.components.ButtonComponent
import com.example.merceariadamaria.components.ClickableTextComponent
import com.example.merceariadamaria.components.DividerTextComponent
import com.example.merceariadamaria.components.MyTextField
import com.example.merceariadamaria.components.NormalTextComponent
import com.example.merceariadamaria.components.PasswordTextField

@Composable
fun LoginScreen(
    onSignupSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
    ) {
        NormalTextComponent(text = stringResource(R.string.hello))

        Spacer(modifier = Modifier.height(20.dp))

        MyTextField(
            labelValue = stringResource(R.string.phone),
            painterResource = painterResource(id = R.drawable.phone_24)
        )
        PasswordTextField(
            labelValue = stringResource(R.string.password),
            painterResource = painterResource(id = R.drawable.password_24)
        )

        Spacer(modifier = Modifier.height(18.dp))

        ButtonComponent(
            value = stringResource(R.string.login),
        )

        Spacer(modifier = Modifier.height(20.dp))

        DividerTextComponent()

        Spacer(modifier = Modifier.height(20.dp))

        ClickableTextComponent(initialText = "Não tem uma conta? ", clickableText = "Registrar", onTextSelected = onSignupSelected)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onSignupSelected = {}
    )
}