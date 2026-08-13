package com.heracles.troco.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heracles.troco.ui.screen.LoginScreen
import com.heracles.troco.ui.screen.PrivacyPoliticsScreen
import com.heracles.troco.ui.screen.SignUpScreen
import com.heracles.troco.ui.screen.TermsAndConditionsScreen

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLITICS = "privacy_politics"
}

@Composable
fun MerceariaApp() {
    val navController = rememberNavController()

    val startDestination = remember { Routes.LOGIN }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize().background(Color.White).statusBarsPadding()
    ) {
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onTermsAndConditions = { navController.navigate(Routes.TERMS_AND_CONDITIONS) },
                onPrivacyPolitics = { navController.navigate(Routes.PRIVACY_POLITICS) },
                onLoginSelected = { navController.navigate(Routes.LOGIN) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignupSelected = { navController.navigate(Routes.SIGN_UP) }
            )
        }

        composable(Routes.TERMS_AND_CONDITIONS) {
            TermsAndConditionsScreen()
        }

        composable(Routes.PRIVACY_POLITICS) {
            PrivacyPoliticsScreen()
        }
    }
}