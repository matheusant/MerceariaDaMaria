//package com.example.merceariadamaria.legacy.app
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//
//@Composable
//fun MerceariaApp() {
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = Color.White
//    ) {
//        Crossfade(targetState = MerceariaRouter.currentScreen, label = "") { currentState ->
//            when (currentState.value) {
//                is Screen.SignUp -> SignUpScreenMerceariaAppNavHost()
//                is Screen.TermsAndConditions -> TermsAndConditionsScreen()
//            }
//        }
//    }
//}