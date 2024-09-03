package com.example.merceariadamaria.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.merceariadamaria.navigation.MerceariaRouter
import com.example.merceariadamaria.navigation.Screen
import com.example.merceariadamaria.screens.SignUpScreen
import com.example.merceariadamaria.screens.TermsAndConditionsScreen

@Composable
fun MerceariaApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Crossfade(targetState = MerceariaRouter.currentScreen, label = "") { currentState ->
            when (currentState.value) {
                is Screen.SignUp -> SignUpScreen()
                is Screen.TermsAndConditions -> TermsAndConditionsScreen()
            }
        }
    }
}