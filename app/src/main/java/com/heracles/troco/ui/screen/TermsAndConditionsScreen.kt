package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.heracles.troco.components.HeaderTextComponent

@Composable
fun TermsAndConditionsScreen() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        HeaderTextComponent(text = "Termos de Uso")
    }
}

@Preview(backgroundColor = 0xFFF, showBackground = true)
@Composable
fun TermsAndConditionsScreenPreview() {
    TermsAndConditionsScreen()
}
