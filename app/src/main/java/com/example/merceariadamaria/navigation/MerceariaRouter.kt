package com.example.merceariadamaria.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

sealed class Screen() {
    object SignUp : Screen()
    object TermsAndConditions : Screen()
}

object MerceariaRouter {

    var currentScreen: MutableState<Screen> = mutableStateOf(Screen.SignUp)

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }
}