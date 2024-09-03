package com.example.merceariadamaria.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merceariadamaria.R
import com.example.merceariadamaria.components.ButtonComponent
import com.example.merceariadamaria.components.CheckboxComponent
import com.example.merceariadamaria.components.ClickableLoginTextComponent
import com.example.merceariadamaria.components.DividerTextComponent
import com.example.merceariadamaria.components.HeaderTextComponent
import com.example.merceariadamaria.components.MyTextField
import com.example.merceariadamaria.components.NormalTextComponent
import com.example.merceariadamaria.components.PasswordTextField
import com.example.merceariadamaria.navigation.MerceariaRouter
import com.example.merceariadamaria.navigation.Screen

@Composable
fun SignUpScreen() {
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

            Spacer(modifier = Modifier.height(10.dp))

            CheckboxComponent("", onTextSelected = {
                MerceariaRouter.navigateTo(Screen.TermsAndConditions)
            })

            Spacer(modifier = Modifier.height(80.dp))

            ButtonComponent(
                value = stringResource(R.string.register),
            )

            Spacer(modifier = Modifier.height(20.dp))

            DividerTextComponent()

            Spacer(modifier = Modifier.height(20.dp))

            ClickableLoginTextComponent {  }
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}