package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heracles.troco.R
import com.heracles.troco.components.ButtonComponent
import com.heracles.troco.components.CheckboxComponent
import com.heracles.troco.components.ClickableTextComponent
import com.heracles.troco.components.ClickableTermsPoliticsTextComponent
import com.heracles.troco.components.DividerTextComponent
import com.heracles.troco.components.HeaderTextComponent
import com.heracles.troco.components.MyTextField
import com.heracles.troco.components.NormalTextComponent
import com.heracles.troco.components.PasswordTextField

@Composable
fun SignUpScreen(
    onLoginSelected: () -> Unit,
    onTermsAndConditions: () -> Unit,
    onPrivacyPolitics: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            NormalTextComponent(text = stringResource(R.string.hello))
            HeaderTextComponent(text = stringResource(R.string.create_account))

            Spacer(modifier = Modifier.height(20.dp))

            MyTextField(
                labelValue = stringResource(R.string.first_name),
                painterResource(id = R.drawable.person_24)
            )
            MyTextField(
                labelValue = stringResource(R.string.last_name),
                painterResource = painterResource(id = R.drawable.person_24)
            )
            MyTextField(
                labelValue = stringResource(R.string.phone),
                painterResource = painterResource(id = R.drawable.phone_24)
            )
            PasswordTextField(
                labelValue = stringResource(R.string.password),
                painterResource = painterResource(id = R.drawable.password_24)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckboxComponent()

                ClickableTermsPoliticsTextComponent(
                    onTermsSelected = onTermsAndConditions,
                    onPrivacyPoliticsSelected = onPrivacyPolitics
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            ButtonComponent(
                value = stringResource(R.string.register),
            )

            Spacer(modifier = Modifier.height(20.dp))

            DividerTextComponent()

            Spacer(modifier = Modifier.height(20.dp))

            ClickableTextComponent(
                initialText = "Já tem uma conta? ",
                clickableText = "Entrar",
                onTextSelected = onLoginSelected
            )
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onTermsAndConditions = {}, onPrivacyPolitics = {}, onLoginSelected = {})
}