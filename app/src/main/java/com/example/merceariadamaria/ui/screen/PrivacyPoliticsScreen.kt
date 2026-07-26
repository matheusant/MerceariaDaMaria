package com.example.merceariadamaria.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.merceariadamaria.components.HeaderTextComponent

@Composable
fun PrivacyPoliticsScreen() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        HeaderTextComponent(text = "Politícas de privacidade")
    }
}

@Preview(backgroundColor = 0xFFF, showBackground = true)
@Composable
fun PrivacyPoliticsScreenPreview() {
    PrivacyPoliticsScreen()
}
