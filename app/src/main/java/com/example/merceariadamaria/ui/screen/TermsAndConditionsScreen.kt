package com.example.merceariadamaria.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merceariadamaria.components.HeaderTextComponent

@Composable
fun TermsAndConditionsScreen() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        color = Color.White
    ) {
        HeaderTextComponent(text = "Termos de Uso")
    }
}

@Preview
@Composable
fun TermsAndConditionsScreenPreview() {
    TermsAndConditionsScreen()
}
